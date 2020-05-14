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
import java.util.LinkedList;
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
    protected List<String> hashedPublicKeys = new LinkedList<>();
    protected ArrayList<Block> readyToMineBlocks = new ArrayList<>();
    protected ArrayList<Transaction> transactions = new ArrayList<>();
    protected Thread blockConsumerThread;
    protected Thread blockProducerThread;
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
        if(leader){
            request(REQUEST_PUBLICKEYS);
            ledger = new Ledger();
        }
        initializeBlockProducerService();
        initializeBlockConsumerService();
        initializeBlockAdderToLedgerService();
        sendPublickey();
    }

    private void initializeBlockAdderToLedgerService(){
        System.out.println("Init Block Adder Service");
        this.blockAdder = new BlockAdder(this.addBlocksToLedgerQueue, this.blockConsumer, this.blockProducer, this.ledger, this.process);
        this.blockAdderThread = new Thread(this.blockAdder);
        this.blockAdderThread.start();
    }

    private void initializeBlockProducerService(){
        System.out.println("Init Block Producer Service");
        this.blockProducer = new BlockProducer(this.readyToMineBlocks, this.transactions, this.blockSize,
                rsa, leader, this.blockConsumerThread,
                this.hashedPublicKeys, this.numOfParticipants, this.ledger, this.process);
        sendLedger();
        this.blockProducerThread = new Thread(this.blockProducer);
        blockProducerThread.start();
    }

    private void initializeBlockConsumerService(){
        System.out.println("Init Block Consumer Service");
        this.blockConsumer.setParams(this.readyToMineBlocks, this.process, this.ledger, this.transactions);
        this.blockConsumerThread = new Thread(this.blockConsumer);
        blockConsumerThread.start();
    }
    private void initializeSubscriptions(){
        System.out.println("Init Subscriptions");
        Subscription.getSubscription().subscribe(Events.TRANSACTION, this);
        Subscription.getSubscription().subscribe(REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(RECEIVE_LEDGER, this);
        Subscription.getSubscription().subscribe(BLOCK, this);
        Subscription.getSubscription().subscribe(Events.PUBLISH_PUBLICKEY, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_PUBLICKEYS, this);
        Subscription.getSubscription().subscribe(Events.RECEIVE_PUBLICKEYS, this);

    }
    private void initializeNetwork() {
        System.out.println("Init the network");
        try {

            InetAddress inetAddress = InetAddress.getByName(address);
            process = new Process(port, inetAddress);
            process.start();
            CommunicationUnit cu = new CommunicationUnit();
            cu.setServerAddress("127.0.0.1");
            cu.setSocketAddress("127.0.0.1");
            cu.setSocketPort(4000);
            cu.setServerPort(port);
            cu.setEvent(ADDRESS);
            process.invokeClientEvent(cu);
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

//    protected void sendPublickKeys() {
//        CommunicationUnit cu = new CommunicationUnit();
//        cu.setEvent(Events.RECEIVE_PUBLICKEYS);
//        cu.setHashedPublicKeys(hashedPublicKeys);
//        process.invokeClientEvent(cu);
//    }

    protected String getHashedPublicKey(){
        ArrayList<byte[]> tmp = new ArrayList<>();
        tmp.add(publicKey.toByteArray());
        tmp.add(modulus.toByteArray());
        byte[] pkHash = new byte[0];
        try {
            pkHash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return BytesConverter.byteToHexString(pkHash, 64);
    }
    protected void sendPublickey() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(RECEIVE_PUBLICKEYS);
        cu.setHashedPublicKey(getHashedPublicKey());
        process.invokeClientEvent(cu);
    }

    @Override
    public void notify(Events event, CommunicationUnit cu) { }
}
