package Nodes.Consensus;

import DataStructures.Block.Block;
import network.Process;
import network.entities.CommunicationUnit;

import java.util.ArrayList;

import static network.events.Events.BFT_REQUEST_ELECTION;

public class BFTBlockConsumer extends Consensus {
    private ArrayList<Block> blocks;
    private CommunicationUnit cu = new CommunicationUnit();
    private Process process;

    public BFTBlockConsumer(){

    }

    public void setParams(ArrayList<Block> blocks, Process process){
        this.blocks = blocks;
        cu.setEvent(BFT_REQUEST_ELECTION);
        this.process = process;
    }

    @Override
    public void run() {
        while (true){
            synchronized (this){
                while (this.blocks.size() == 0) {
                    try {
                        System.out.println("BFT Consensus: Waiting for a block");
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("BFT Consensus: Block received start working...");
                    }
                }
                Block currentMiningBlock = this.blocks.get(0);
                this.blocks.remove(currentMiningBlock);
                currentMiningBlock.getHeader().hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
                this.process.invokeClientEvent(cu);
                try {
                    System.out.println("BFT Consensus: Waiting for vote to finish...");
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("BFT Consensus: Vote finished");
                }

            }
        }
    }
}
