package Nodes;

import DataStructures.Block.Block;
import DataStructures.Block.BlockHeader;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import Utils.BytesConverter;
import Utils.RSA;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import static network.events.Events.*;


//TODO when to use Block.isValid

public class POWMiner implements Subscription.Subscriber{
    private Consensus blockConsumer;
    private BlockProducer blockProducer;
    private Ledger ledger;
    private Process process;
    private Block block;
    private int blockSize;
    private ArrayList<Block> readyToMineBlocks = new ArrayList<>();
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private String address;
    int port;
    private RSA rsa = new RSA(2048);
    private HashMap<String, Integer> transactionHashToIndex = new HashMap<>();
    private boolean leader;
    private Thread blockConsumerThread;
    private Thread blockProducerThread;
    public POWMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader){
        this.address = address;
        this.port = port;
        this.blockConsumer = blockConsumer;
        this.blockSize = blockSize;
        this.leader = leader;
        initializeNetwork();
        initializeSubscriptions();
        initializeBlockProducer();
        initializeBlockConsumer();
    }

    private void initializeBlockProducer(){
        this.blockProducer = new BlockProducer(this.readyToMineBlocks, this.transactions, this.blockSize, rsa, leader, this.blockConsumerThread);
        this.blockProducerThread = new Thread(this.blockProducer);
        blockProducerThread.start();
    }

    private void initializeBlockConsumer(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.broadcastCommUnit(), this.process, this.ledger, this.transactions);
        this.blockConsumerThread = new Thread(this.blockConsumer);
        blockConsumerThread.start();
    }
    private void initializeSubscriptions(){
        Subscription.getSubscription().subscribe(Events.TRANSACTION, this);
        Subscription.getSubscription().subscribe(REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(RECEIVE_LEDGER, this);
        Subscription.getSubscription().subscribe(BLOCK, this);
    }
    private void initializeNetwork() {
        try {

            InetAddress inetAddress = InetAddress.getByName(address);
            process = new Process(port, inetAddress);
            process.start();

            requestLedger();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void initializeGenesisBlock(){
        BlockHeader header = new BlockHeader();
        header.hashOfPrevBlock = new byte[]{0};
        this.block = new Block(this.blockSize);
        block.setHeader(header);
    }

    private void initializeBlock(){
        BlockHeader header = new BlockHeader();
//        header.hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
        this.block = new Block(this.blockSize);
        block.setHeader(header);
    }

    private void requestLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(REQUEST_LEDGER);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        process.invokeClientEvent(cu);
    }
    @Override
    public void notify(Events event, CommunicationUnit cu) {
        switch (event) {
            case TRANSACTION:
                serveTransactionEvent(cu);
                break;
            case RECEIVE_LEDGER:
                ledger = cu.getLedger();
                break;
            case BLOCK:
                this.addBlock(cu.getBlock());
                break;

            case REQUEST_LEDGER:
                sendLedger();
                break;
        }
    }

    private void addBlock(Block block){
        try {
            boolean success = this.ledger.addBlock(block);
            if(success){
                this.blockConsumer.StopMiningCurrentBlock(block);
                this.blockProducer.setInterrupt(block);
            }
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_LEDGER);
        cu.setLedger(ledger);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        process.invokeClientEvent(cu);
    }

    private CommunicationUnit broadcastCommUnit(){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BLOCK);
        cu.setBlock(this.block);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        return cu;
    }
    private boolean repeatedTransaction(Transaction transaction){
        String hash = null;
        try {
            hash = BytesConverter.byteToHexString(
                    transaction.getTransactionHash(),64);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return transactionHashToIndex.containsKey(hash);
    }

    private void serveTransactionEvent(CommunicationUnit cu){
        if(!repeatedTransaction(cu.getTransaction())){
            this.transactions.add(cu.getTransaction());
            if(this.transactions.size() == 1){
                this.blockProducerThread.interrupt();
            }
            try {
                transactionHashToIndex.put(BytesConverter.byteToHexString(
                        cu.getTransaction().getTransactionHash(),64), this.transactions.size()-1);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
