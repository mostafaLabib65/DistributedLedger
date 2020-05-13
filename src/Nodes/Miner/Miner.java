package Nodes.Miner;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import Nodes.Consensus.Consensus;
import Nodes.MinerUtils.BlockAdder;
import Nodes.MinerUtils.BlockProducer;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static network.events.Events.*;

public abstract class Miner implements Subscription.Subscriber{
    protected Consensus blockConsumer;
    protected BlockProducer blockProducer;
    protected BlockAdder blockAdder;
    protected Ledger ledger;
    protected Process process;
    private int blockSize;
    protected String address;
    protected int port;
    protected boolean leader;
    private RSA rsa = new RSA(2048);
    private BigInteger publicKey = rsa.getPublicKey();
    private BigInteger modulus = rsa.getModulus();
    private HashMap<String, Integer> transactionHashToIndex = new HashMap<>();
    protected List<String> hashedPublicKeys;
    protected ArrayList<Block> readyToMineBlocks = new ArrayList<>();
    protected ArrayList<Transaction> transactions = new ArrayList<>();
    protected Thread blockConsumerThread;
    private Thread blockProducerThread;
    protected Thread blockAdderThread;
    protected int numOfParticipants;
    protected ArrayList<Block> addBlocksToLedgerQueue = new ArrayList<>();
    public Miner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader, int numOfParticipants){
        this.address = address;
        this.port = port;
        this.blockConsumer = blockConsumer;
        this.blockSize = blockSize;
        this.leader = leader;
        this.numOfParticipants = numOfParticipants;
        initializeNetwork();
        initializeSubscriptions();
        initializeBlockProducerService();
        initializeBlockConsumerService();
        initializeBlockAdderToLedgerService();
        sendPublickey();
        if(leader){
            request(REQUEST_PUBLICKEYS);
        }
    }

    private void initializeBlockAdderToLedgerService(){
        this.blockAdder = new BlockAdder(this.addBlocksToLedgerQueue, this.blockConsumer, this.blockProducer, this.ledger, this.process);
        this.blockAdderThread = new Thread(this.blockAdder);
        this.blockAdderThread.start();
    }

    private void initializeBlockProducerService(){
        this.blockProducer = new BlockProducer(this.readyToMineBlocks, this.transactions, this.blockSize, rsa, leader, this.blockConsumerThread, this.hashedPublicKeys, this.numOfParticipants);
        try {
            ledger.addBlock(blockProducer.getGenesisBlock());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sendLedger();
        this.blockProducerThread = new Thread(this.blockProducer);
        blockProducerThread.start();
    }

    private void initializeBlockConsumerService(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.process, this.ledger, this.transactions);
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void request(Events event) {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(event);
        process.invokeClientEvent(cu);
    }

    protected void sendLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_LEDGER);
        cu.setLedger(ledger);
        process.invokeClientEvent(cu);
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

    protected void serveTransactionEvent(CommunicationUnit cu){
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

//    protected void addBlock(Block block){
//        try {
//            boolean success = this.ledger.addBlock(block);
//            if(success){
//                this.blockConsumer.StopMiningCurrentBlock(block);
//                this.blockProducer.setInterrupt(block);
//            }else {
//
//            }
//        }catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }

    protected void sendPublickKeys() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_PUBLICKEYS);
        cu.setHashedPublicKeys(hashedPublicKeys);
        process.invokeClientEvent(cu);
    }

    private void sendPublickey() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.PUBLISH_PUBLICKEY);
        ArrayList<byte[]> tmp = new ArrayList<>();
        tmp.add(publicKey.toByteArray());
        tmp.add(modulus.toByteArray());
        byte[] pkHash = new byte[0];
        try {
            pkHash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        cu.setHashedPublicKey(BytesConverter.byteToHexString(pkHash, 64));
        process.invokeClientEvent(cu);
    }

    @Override
    public void notify(Events event, CommunicationUnit cu) { }
}
