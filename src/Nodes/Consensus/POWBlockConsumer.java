package Nodes.Consensus;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.Process;
import network.entities.CommunicationUnit;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static network.events.Events.BLOCK;

public class POWBlockConsumer extends Consensus {
    private int difficulty;
    private ArrayList<Block> blocks;
    private CommunicationUnit cu = new CommunicationUnit();
    private Process process;
    public POWBlockConsumer(int difficulty){
        this.difficulty = difficulty;
    }
    private boolean interrupt;
    private Ledger ledger;
    private Block receivedBlock;
    private Block currentMiningBlock;
    private int nonce;
    private boolean blockCorrupted = false;
    private ArrayList<Transaction> allTransactions;
    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger, ArrayList<Transaction> transactions){
        this.blocks = blocks;
        this.process = process;
        this.ledger = ledger;
        this.allTransactions = transactions;
        cu.setEvent(BLOCK);
    }

    private boolean isValidPOWBlock(Block block, int difficulty) throws NoSuchAlgorithmException {
        BigInteger hash = new BigInteger(1, block.getHeader().hashBlockHeader());
        BigInteger b1 = new BigInteger("2");
        int exponent = 256 - difficulty;
        BigInteger limit = b1.pow(exponent);
        return hash.compareTo(limit) < 0;
    }
    public void StopMiningCurrentBlock(Block receivedBlock){
        this.interrupt = true;
        this.receivedBlock = receivedBlock;
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
                    if(blockCorrupted){
                        this.currentMiningBlock = this.blocks.get(0);
                        this.blocks.remove(currentMiningBlock);
                        currentMiningBlock.getHeader().hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
                        nonce = 0;
                    }
                    this.interrupt = false;
                    this.blockCorrupted = false;
                    do{
                        currentMiningBlock.getHeader().nonce = nonce;
                        nonce++;
                    }while (!isValidPOWBlock(currentMiningBlock, this.difficulty) && !interrupt);
                    if(interrupt){
                        Transaction[] transactions = currentMiningBlock.getTransactions();
                        for(Transaction acceptedTransaction: receivedBlock.getTransactions()){
                            int index = currentMiningBlock.getIndexOfTransaction(acceptedTransaction);
                            if(index != -1){
                                transactions[index] = null;
                                blockCorrupted = true;
                            }
                        }
                        if(blockCorrupted){
                            for(Transaction t: transactions){
                                if(t != null){
                                    allTransactions.add(0, t);
                                }
                            }
                        }
                    }else {
                        boolean success = this.ledger.addBlock(currentMiningBlock);
                        if(success){
                            cu.setBlock(currentMiningBlock);
                            this.process.invokeClientEvent(cu);
                        }
                    }
                }catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
