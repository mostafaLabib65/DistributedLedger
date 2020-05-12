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

public class BFTMiner implements Subscription.Subscriber{
    private Consensus blockConsumer;
    private BlockProducer blockProducer;
    private VotingSystemLeader votingSystemLeader;
    private Ledger ledger;
    private Process process;
    private Block block;
    private int blockSize;
    private ArrayList<Block> readyToMineBlocks = new ArrayList<>();
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private String address;
    private int port;
    private int numOfParticipants;
    private RSA rsa = new RSA(2048);
    private HashMap<String, Integer> transactionHashToIndex = new HashMap<>();
    private boolean leader;
    private ArrayList<Block> votingBlocksQueue = new ArrayList<>();
    private VotingUnit votingUnit;
    private int ID;
    private Thread blockConsumerThread;
    private Thread blockProducerThread;
    private Thread votingSystemLeaderThread;
    public BFTMiner(Consensus blockConsumer, int blockSize, String address, int port, int numOfParticipants, boolean leader, int ID){
        this.address = address;
        this.port = port;
        this.blockConsumer = blockConsumer;
        this.blockSize = blockSize;
        this.numOfParticipants = numOfParticipants;
        this.leader = leader;
        this.ID = ID;
        initializeNetwork();
        initializeSubscriptions();
        initializeBlockProducer();
        initializeBlockConsumer();
        if(leader)
            initializeBlockVotingSystem();
    }

    private void initializeBlockVotingSystem(){
        this.votingSystemLeader = new VotingSystemLeader(votingBlocksQueue, this.process, this.votingBroadcast());
        this.votingSystemLeaderThread = new Thread(this.votingSystemLeader);
        votingSystemLeaderThread.start();
    }
    private void initializeBlockProducer(){
        this.blockProducer = new BlockProducer(this.readyToMineBlocks, this.transactions, this.blockSize, this.rsa, this.leader, this.blockConsumerThread);
        this.blockProducerThread = new Thread(this.blockProducer);
        blockProducerThread.start();
    }

    private void initializeBlockConsumer(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.broadcastCommUnit(), this.process);
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





    @Override
    public void notify(Events event, CommunicationUnit cu) {
        switch (event) {
            case TRANSACTION:
                serveTransactionEvent(cu);
                break;

            case BFT_REQUEST_ELECTION:
                if(leader){
                    this.votingBlocksQueue.add(cu.getBlock());
                }
                break;

            case BFT_REQUEST_VOTE:
                this.request_vote(cu.getBlock());
                break;

            case BFT_RECEIVE_VOTE:
                this.receive_vote(cu.getBFTVote());
                break;

            case RECEIVE_LEDGER:
                ledger = cu.getLedger(); //TODO check what to accept
                break;

            case REQUEST_LEDGER:
                sendLedger();
                break;
        }
    }

    private void request_vote(Block block){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_RECEIVE_VOTE);
        votingUnit = new VotingUnit(block, numOfParticipants);
        int result = votingUnit.addVote(true);  //TODO this.leader.canBeAdded(block)
        if(result == 1){
            addBlock(block);
        }
        cu.setBFTVote(true); //TODO this.leader.canBeAdded(block)
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        process.invokeClientEvent(cu);
    }

    private void receive_vote(boolean vote){
        if(votingUnit != null){
            int result = votingUnit.addVote(vote);
            if(result == 1){
                addBlock(votingUnit.getBlock());
            }else if(result == 0 && leader){
                this.votingSystemLeader.setReceivedBlock(block);
                votingSystemLeaderThread.interrupt();
            }
        }
    }

    private void serveTransactionEvent(CommunicationUnit cu){
        if(!repeatedTransaction(cu.getTransaction())){
            this.transactions.add(cu.getTransaction());
            if(transactions.size() == 1){
                blockProducerThread.interrupt();
            }
            try {
                transactionHashToIndex.put(BytesConverter.byteToHexString(
                        cu.getTransaction().getTransactionHash(),64), this.transactions.size()-1);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
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

    private void requestLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(REQUEST_LEDGER);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        process.invokeClientEvent(cu);
    }

    private void addBlock(Block block){
        try {
            boolean success = this.ledger.addBlock(block); //TODO
            if(success){
                this.blockProducer.setInterrupt(block);
            }
            if(leader){
                this.votingSystemLeader.setReceivedBlock(block);
                votingSystemLeaderThread.interrupt();
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
        cu.setEvent(BFT_REQUEST_ELECTION);
        cu.setBlock(this.block);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        return cu;
    }

    private CommunicationUnit votingBroadcast(){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_REQUEST_VOTE);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        return cu;
    }


}
