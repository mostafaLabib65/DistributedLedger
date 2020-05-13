package Nodes.MinerUtils;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import Nodes.Consensus.Consensus;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BlockAdder implements Runnable{

    private ArrayList<Block> addBlocksToLedgerQueue;
    private Consensus blockConsumer;
    private BlockProducer blockProducer;
    private Ledger ledger;
    private Process process;
    private boolean waitingForLedger;
    private Block block;
    public BlockAdder(ArrayList<Block> addBlocksToLedgerQueue, Consensus blockConsumer, BlockProducer blockProducer, Ledger ledger, Process process){
        this.addBlocksToLedgerQueue = addBlocksToLedgerQueue;
        this.blockConsumer = blockConsumer;
        this.blockProducer = blockProducer;
        this.ledger = ledger;
        this.process = process;
    }



    @Override
    public void run() {
        while (true){
            synchronized (this){
                if(addBlocksToLedgerQueue.size() == 0) {
                    try {
                        System.out.println("BlockAdderThread: Waiting for blocks");

                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("BlockAdderThread: Block received start adding to ledger");
                    }
                }
                if(!waitingForLedger){
                    this.block = addBlocksToLedgerQueue.get(0);
                    addBlocksToLedgerQueue.remove(block);
                }
                waitingForLedger = false;
                try {
                    boolean success = this.ledger.addBlock(block);
                    if(success){
                        this.blockConsumer.StopMiningCurrentBlock(block);
                        this.blockProducer.setInterrupt(block);
                    }else {
                        CommunicationUnit cu = new CommunicationUnit();
                        cu.setEvent(Events.REQUEST_LEDGER);
                        process.invokeClientEvent(cu);
                        System.out.println("Block Adder: Waiting for a ledger");
                        waitingForLedger = true;
                        wait();
                    }
                }catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("Block Adder: New Ledger received- try to add block");
                }
            }
        }
    }
}
