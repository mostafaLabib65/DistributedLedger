package Nodes.Consensus;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import network.Process;
import network.entities.CommunicationUnit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static network.events.Events.BFT_REQUEST_ELECTION;

public class BFTBlockConsumer extends Consensus {
    private ArrayList<Block> blocks;
    private CommunicationUnit cu = new CommunicationUnit();
    private Process process;
    private Ledger ledger;
    public BFTBlockConsumer(){

    }

    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger){
        this.blocks = blocks;
        cu.setEvent(BFT_REQUEST_ELECTION);
        this.process = process;
        this.ledger = ledger;
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
                try {
                    currentMiningBlock.getHeader().hashOfPrevBlock = this.ledger.getLastBlockHash();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
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
