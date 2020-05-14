package Nodes.Miner;

import DataStructures.Block.Block;
import Nodes.Consensus.Consensus;
import Nodes.MinerUtils.Configs;
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
    private VotingUnit votingUnit;

    public BFTMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader, int numOfParticipants) {
        super(blockConsumer, blockSize, address, port, leader, numOfParticipants);
        initializeSubscriptions();
        initializeBlockConsumer();
        if(leader)
            initializeBlockVotingSystem();
    }


    private void initializeBlockConsumer(){
        this.blockConsumer.setParams(this.readyToMineBlocks, this.process, this.ledger);
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
        return cu;
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
        }else if(result == Configs.REJECTED && leader){
            this.votingSystemLeader.setReceivedBlock(votingUnit.getBlock());
            votingSystemLeaderThread.interrupt();
        }
    }

    private void receive_vote(boolean vote){
        if(votingUnit != null){
            Configs result = votingUnit.addVote(vote);
            addBlockToQueue(result);
        }
    }

    private void request_vote(Block block){
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(BFT_RECEIVE_VOTE);
        votingUnit = new VotingUnit(block, numOfParticipants);
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
                }
                break;

            case BFT_REQUEST_VOTE:
                this.request_vote(cu.getBlock());
                break;

            case BFT_RECEIVE_VOTE:
                this.receive_vote(cu.getBFTVote());
                break;

            case RECEIVE_LEDGER:
                if(cu.getLedger().getLegderDepth() >= ledger.getLegderDepth()){
                    ledger = cu.getLedger();
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
                System.out.println("POW Miner: received public key");
                hashedPublicKeys.add(cu.getHashedPublicKey());
                if(hashedPublicKeys.size() == numOfParticipants-1)
                    hashedPublicKeys.add(getHashedPublicKey());
                this.blockProducerThread.interrupt();
                break;
        }
    }
}
