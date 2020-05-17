package Nodes.MinerUtils;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import Nodes.Consensus.Consensus;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class BlockAdder implements Runnable{

    private ArrayList<Block> addBlocksToLedgerQueue;
    private Consensus blockConsumer;
    private BlockProducer blockProducer;
    private Ledger ledger;
    private Process process;
    public boolean waitingForLedger = false;
    private Block block;
    private VotingUnit votingUnit;
    private Thread blockConsumerThread;
    private boolean waitingForElection = false;
    private int numOfTries = 0;
    public BlockAdder(ArrayList<Block> addBlocksToLedgerQueue, Consensus blockConsumer,
                      BlockProducer blockProducer, Ledger ledger, Process process){
        this.addBlocksToLedgerQueue = addBlocksToLedgerQueue;
        this.blockConsumer = blockConsumer;
        this.blockProducer = blockProducer;
        this.ledger = ledger;
        this.process = process;
    }

    public BlockAdder(ArrayList<Block> addBlocksToLedgerQueue, Consensus blockConsumer,
                      BlockProducer blockProducer, Ledger ledger, Process process, Thread blockConsumerThread){
        this.addBlocksToLedgerQueue = addBlocksToLedgerQueue;
        this.blockConsumer = blockConsumer;
        this.blockProducer = blockProducer;
        this.ledger = ledger;
        this.process = process;
        this.blockConsumerThread = blockConsumerThread;
    }

    public void setWaitingForElection(){
        this.waitingForElection = true;
    }
    public void setLedger(Ledger ledger){
        numOfTries++;
        this.ledger = ledger;
    }
    public void setVotingUnit(VotingUnit votingUnit){
        this.votingUnit = votingUnit;
    }
    private void addBlockToLedger(Block block){
        try {
            boolean success = this.ledger.addBlock(block);
            if(success){
                this.blockConsumer.StopMiningCurrentBlock(block);
                this.blockProducer.setInterrupt(block);
                System.out.println("Block Adder.............................." + currentTimeMillis() + " Ledger length: " + ledger.getLedgerDepth());
            }else if(numOfTries != 3){
                CommunicationUnit cu = new CommunicationUnit();
                cu.setEvent(Events.REQUEST_LEDGER);
                process.invokeClientEvent(cu);
                System.out.println("Block Adder: Waiting for new ledger");
                waitingForLedger = true;
                wait();
            }else {
                waitingForLedger = false;
                numOfTries = 1;
            }
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Block Adder: New Ledger received- try to add block");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void run() {
        Block block = null;
        while (true){
            synchronized (this){
                if(addBlocksToLedgerQueue.size() == 0 && !waitingForLedger) {
                    try {
                        System.out.println("BlockAdderThread: Waiting for blocks");

                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("BlockAdderThread: Block received start adding to ledger");
                    }
                }
                if(!waitingForLedger && addBlocksToLedgerQueue.size() != 0){
                    block  = addBlocksToLedgerQueue.get(0);
                    addBlocksToLedgerQueue.remove(block);
                    addBlockToLedger(block);
                }
                if(waitingForLedger){
                    waitingForLedger = false;
                    addBlockToLedger(block);
                }
            }
        }
    }
}
