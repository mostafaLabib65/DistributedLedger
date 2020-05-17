package Nodes.Consensus;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import Nodes.MinerUtils.BlockAdder;
import network.Process;
import network.entities.CommunicationUnit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static network.events.Events.BFT_REQUEST_ELECTION;

public class BFTBlockConsumer extends Consensus {
    private ArrayList<Block> blocks;
    private CommunicationUnit cu = new CommunicationUnit();
    private Process process;
    private Ledger ledger;
    private boolean waitingForVote  = false;
    private boolean leader;
    private ArrayList<Block> votingBlocksQueue;
    private Thread votingSystemLeaderThread;
    private BlockAdder blockAdder;
    private Block currentMiningBlock;
    private Block receivedBlock;
    private ReentrantLock readyToMineBlocksQueueLock;
    public BFTBlockConsumer(){

    }

    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger, boolean leader,
                          ArrayList<Block> votingBlocksQueue, Thread votingSystemLeaderThread, ReentrantLock readyToMineBlocksQueueLock){
        this.blocks = blocks;
        cu.setEvent(BFT_REQUEST_ELECTION);
        this.process = process;
        this.ledger = ledger;
        this.leader = leader;
        this.votingBlocksQueue = votingBlocksQueue;
        this.votingSystemLeaderThread = votingSystemLeaderThread;
        this.readyToMineBlocksQueueLock = readyToMineBlocksQueueLock;
    }

    public void setBlockAdder(BlockAdder blockAdder){
        this.blockAdder = blockAdder;
    }
    public void setReceivedBlock(Block receivedBlock){
        this.receivedBlock = receivedBlock;
    }

    @Override
    public void run() {
        while (true){
            synchronized (this){
                readyToMineBlocksQueueLock.lock();
                int size = this.blocks.size();
                readyToMineBlocksQueueLock.unlock();
                if (size == 0 || ledger == null) {
                    try {
                        System.out.println("BFT Consensus: Waiting for a block");
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("BFT Consensus: Block received start working...");
                    }
                }
                readyToMineBlocksQueueLock.lock();
                size = this.blocks.size();
                readyToMineBlocksQueueLock.unlock();

                if(!waitingForVote && size != 0){
                    System.out.println("BFT Consensus: Vote on new block...");
                    readyToMineBlocksQueueLock.lock();
                    this.currentMiningBlock = this.blocks.get(0);
                    this.blocks.remove(currentMiningBlock);
                    readyToMineBlocksQueueLock.unlock();
                    try {
                        currentMiningBlock.getHeader().hashOfPrevBlock = this.ledger.getLastBlockHash();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    if(leader){
                        votingBlocksQueue.add(currentMiningBlock);
                        if(votingBlocksQueue.size() == 1){
                            this.votingSystemLeaderThread.interrupt();
                        }
                    }else {
                        cu.setBlock(currentMiningBlock);
                        this.process.invokeClientEvent(cu);
                    }
                    waitingForVote = true;
                }
                try {
                    System.out.println("BFT Consensus: Waiting for vote to finish...");
                    blockAdder.setWaitingForElection();
                    wait();
                } catch (InterruptedException e) {
                    if(waitingForVote){
                        try {
                            if(Arrays.equals(receivedBlock.getHash(), currentMiningBlock.getHash())){
                                waitingForVote = false;
                                System.out.println("BFT Consensus: Vote finished");
                            }
                        } catch (NoSuchAlgorithmException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }
        }
    }
}
