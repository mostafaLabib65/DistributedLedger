package Nodes.Miner;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import Nodes.Consensus.Consensus;
import Nodes.MinerUtils.*;
import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static network.events.Events.*;

public class BFTMiner extends Miner{
    private Thread votingSystemLeaderThread;
    private VotingSystemLeader votingSystemLeader;
    private ArrayList<Block> votingBlocksQueue = new ArrayList<>();
    private VotingUnit votingUnit;

    public BFTMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader, int numOfParticipants) {
        super(blockConsumer, blockSize, address, port, leader, numOfParticipants);
        initializeSubscriptions();
        if(leader){
            initializeBlockVotingSystem();
            ledger = new Ledger();
        }
        initializeBlockConsumer();
        initializeBlockProducer();
        initializeBlockAdderToLedgerService();
        if(leader){
            request(REQUEST_PUBLICKEYS);
        }
        sendPublickey();
    }

    protected void initializeBlockAdderToLedgerService(){
        System.out.println("Init Block Adder Service");
        this.blockAdder = new BlockAdder(this.addBlocksToLedgerQueue, this.blockConsumer,
                this.blockProducer, this.ledger, this.process, this.blockConsumerThread);
        this.blockConsumer.setBlockAdder(this.blockAdder);
        this.blockAdderThread = new Thread(this.blockAdder);
        this.blockAdderThread.start();
    }

    protected void initializeBlockProducer(){
        this.blockProducer = new BlockProducer(this.readyToMineBlocks, this.transactions, this.blockSize,
                this.rsa, leader, this.blockConsumerThread, this.hashedPublicKeys,
                this.numOfParticipants, this.ledger, this.process, this.readyToMineBlocksQueueLock);
        this.blockProducerThread = new Thread(this.blockProducer);
        blockProducerThread.start();
        System.out.println("Init Block Producer Service");
    }

    private void initializeBlockConsumer(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.process, this.ledger, this.leader, this.votingBlocksQueue,
                votingSystemLeaderThread, this.readyToMineBlocksQueueLock);
        this.blockConsumerThread = new Thread(this.blockConsumer);
        blockConsumerThread.start();
    }
    protected void initializeSubscriptions(){
        System.out.println("Init Subscriptions");
        Subscription.getSubscription().subscribe(Events.TRANSACTION, this);
        Subscription.getSubscription().subscribe(REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(RECEIVE_LEDGER, this);
        Subscription.getSubscription().subscribe(BLOCK, this);
        Subscription.getSubscription().subscribe(Events.PUBLISH_PUBLICKEY, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_PUBLICKEYS, this);
        Subscription.getSubscription().subscribe(Events.RECEIVE_PUBLICKEYS, this);
        Subscription.getSubscription().subscribe(BFT_REQUEST_ELECTION, this);
        Subscription.getSubscription().subscribe(BFT_REQUEST_VOTE, this);
        Subscription.getSubscription().subscribe(BFT_RECEIVE_VOTE, this);
    }

    private void initializeBlockVotingSystem(){
        this.votingSystemLeader = new VotingSystemLeader(votingBlocksQueue, this.process, this);
        this.votingSystemLeaderThread = new Thread(this.votingSystemLeader);
        votingSystemLeaderThread.start();
    }

    private CommunicationUnit broadcastBlockCommUnit(){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_REQUEST_ELECTION);
        return cu;
    }

    protected void addBlockToQueue(Configs result){
        if(result == Configs.ACCEPTED){
            this.addBlocksToLedgerQueue.add(votingUnit.getBlock());
            if(this.addBlocksToLedgerQueue.size() == 1){
                this.blockAdderThread.interrupt();
            }

        }
        if(leader){
            this.votingSystemLeader.setReceivedBlock(votingUnit.getBlock());
            votingSystemLeaderThread.interrupt();
        }
        this.blockConsumer.setReceivedBlock(votingUnit.getBlock());
        blockConsumerThread.interrupt();
    }

    private void receive_vote(boolean vote){
        if(votingUnit != null){
            Configs result = votingUnit.addVote(vote);
            addBlockToQueue(result);
        }
    }

    public void request_vote(Block block){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_RECEIVE_VOTE);
        votingUnit = new VotingUnit(block, numOfParticipants);
        this.blockAdder.setVotingUnit(votingUnit);
        try {
            boolean canBeAdded = ledger.isValidBlockForLedger(block);
            Configs result = votingUnit.addVote(canBeAdded);
            cu.setBFTVote(canBeAdded);
            process.invokeClientEvent(cu);
            addBlockToQueue(result);
        } catch (NoSuchAlgorithmException e) {
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
                    if(votingBlocksQueue.size() == 1){
                        this.votingSystemLeaderThread.interrupt();
                    }
                }
                break;

            case BFT_REQUEST_VOTE:
                System.out.println("BFT Miner: received request for vote");
                this.request_vote(cu.getBlock());
                break;

            case BFT_RECEIVE_VOTE:
                System.out.println("BFT Miner: received new vote");
                this.receive_vote(cu.getBFTVote());
                break;

            case RECEIVE_LEDGER:
                System.out.println("BFT Miner: received Ledger");
                if(ledger == null || this.blockAdder.waitingForLedger){
                    System.out.println("BFT Miner: Accepting ledger");
                    ledger = cu.getLedger();
                    this.blockConsumer.setLedger(ledger);
                    this.blockAdder.setLedger(ledger);
                    this.blockAdderThread.interrupt();
                }
                break;

            case REQUEST_LEDGER:
                sendLedger();
                break;



            case REQUEST_PUBLICKEYS:
                this.sendPublickey();
                break;


            case RECEIVE_PUBLICKEYS:
                System.out.println("BFT Miner: received public key");
                hashedPublicKeys.add(cu.getHashedPublicKey());
                if(hashedPublicKeys.size() == numOfParticipants-1)
                    hashedPublicKeys.add(getHashedPublicKey());
                this.blockProducerThread.interrupt();
                break;
        }
    }
}
