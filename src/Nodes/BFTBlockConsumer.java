package Nodes;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.Process;
import network.entities.CommunicationUnit;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BFTBlockConsumer extends Consensus {
    private ArrayList<Block> blocks;
    private CommunicationUnit cu;
    private Process process;

    public BFTBlockConsumer(){

    }

    public void setParams(ArrayList<Block> blocks, CommunicationUnit cu, Process process){
        this.blocks = blocks;
        this.cu = cu;
        this.process = process;
    }

    @Override
    public void run() {
        while (true){
            synchronized (this){
                while (this.blocks.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Block currentMiningBlock = this.blocks.get(0);
                this.blocks.remove(currentMiningBlock);
                currentMiningBlock.getHeader().hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
                this.process.invokeClientEvent(cu);
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
