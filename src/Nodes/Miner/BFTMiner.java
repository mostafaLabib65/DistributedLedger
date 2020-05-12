package Nodes.Miner;

import DataStructures.Block.Block;
import Nodes.Consensus.Consensus;
import Nodes.MinerUtils.VotingSystemLeader;
import Nodes.MinerUtils.VotingUnit;
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
    private int numOfParticipants;
    private VotingUnit votingUnit;

    public BFTMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader, int numOfParticipants) {
        super(blockConsumer, blockSize, address, port, leader);
        this.numOfParticipants = numOfParticipants;
        initializeSubscriptions();
        initializeBlockConsumer();
        if(leader)
            initializeBlockVotingSystem();
    }
    private void initializeBlockConsumer(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.broadcastBlockCommUnit(), this.process);
        this.blockConsumerThread = new Thread(this.blockConsumer);
        blockConsumerThread.start();
    }
    private void initializeSubscriptions(){
        Subscription.getSubscription().subscribe(BFT_REQUEST_ELECTION, this);
        Subscription.getSubscription().subscribe(BFT_REQUEST_VOTE, this);
        Subscription.getSubscription().subscribe(BFT_RECEIVE_VOTE, this);
    }

    private void initializeBlockVotingSystem(){
        this.votingSystemLeader = new VotingSystemLeader(votingBlocksQueue, this.process, this.votingBroadcast());
        this.votingSystemLeaderThread = new Thread(this.votingSystemLeader);
        votingSystemLeaderThread.start();
    }

    private CommunicationUnit votingBroadcast(){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_REQUEST_VOTE);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        return cu;
    }
    private CommunicationUnit broadcastBlockCommUnit(){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_REQUEST_ELECTION);
        cu.setSocketPort(this.port);
        cu.setSocketAddress(this.address);
        return cu;
    }

    protected void addBlock(Block block){
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

    private void receive_vote(boolean vote){
        if(votingUnit != null){
            int result = votingUnit.addVote(vote);
            if(result == 1){
                addBlock(votingUnit.getBlock());
            }else if(result == 0 && leader){
                this.votingSystemLeader.setReceivedBlock(votingUnit.getBlock());
                votingSystemLeaderThread.interrupt();
            }
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
}
