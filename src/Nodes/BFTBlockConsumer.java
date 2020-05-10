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
    private int difficulty;
    private ArrayList<Block> blocks;
    private CommunicationUnit cu;
    private Process process;
    public BFTBlockConsumer(int difficulty){
        this.difficulty = difficulty;
    }
    private boolean interrupt;
    private Ledger ledger;
    private Block receivedBlock;
    private Block currentMiningBlock;
    private int numOfParticipants;
    private boolean voteProcessRunning = false;
    int positiveVotes = 0;
    int negativeVotes = 0;
    public void setParams(ArrayList<Block> blocks, CommunicationUnit cu, Process process, Ledger ledger, int numOfParticipants){
        this.blocks = blocks;
        this.cu = cu;
        this.process = process;
        this.ledger = ledger;
        this.numOfParticipants = numOfParticipants;
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
                try {
                    this.currentMiningBlock = this.blocks.get(0);
                    this.blocks.remove(currentMiningBlock);
                    currentMiningBlock.getHeader().hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
                    if(!voteProcessRunning){
                        if(true){ //TODO validBlock
                            this.process.invokeClientEvent(cu);
                            voteProcessRunning = true;
                        }
                    }

                    while (positiveVotes + negativeVotes < numOfParticipants)
                        wait();

//                    if(interrupt){
//                        for(Transaction acceptedTransaction: receivedBlock.transactions){
//                            for(Transaction testedTransaction: currentMiningBlock.transactions){
//                                //TODO check for equal transactions
//                                //TODO what if i found one
//                            }
//                        }
//                    }else {
//                        boolean success = this.ledger.addBlock(currentMiningBlock);
//                        if(success){
//                            this.process.invokeClientEvent(cu);
//                        }
//                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
