package Nodes;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.Process;
import network.entities.CommunicationUnit;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class POWBlockConsumer extends Consensus {
    private int difficulty;
    private ArrayList<Block> blocks;
    private CommunicationUnit cu;
    private Process process;
    public POWBlockConsumer(int difficulty){
        this.difficulty = difficulty;
    }
    private boolean interrupt;
    private Ledger ledger;
    private Block receivedBlock;
    private Block currentMiningBlock;
    private int nonce;
    public void setParams(ArrayList<Block> blocks, CommunicationUnit cu, Process process, Ledger ledger){
        this.blocks = blocks;
        this.cu = cu;
        this.process = process;
        this.ledger = ledger;
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
                    this.currentMiningBlock = this.blocks.get(0);
                    this.blocks.remove(currentMiningBlock);
                    currentMiningBlock.getHeader().hashOfPrevBlock = null;// = this.ledger.getLastBlockHash(); // TODO Correct it
                    nonce = 0;
                    this.interrupt = false;
                    do{
                        currentMiningBlock.getHeader().nonce = nonce;
                        nonce++;
                    }while (!isValidPOWBlock(currentMiningBlock, this.difficulty) && !interrupt);
                    if(interrupt){
                        for(Transaction acceptedTransaction: receivedBlock.getTransactions()){
                            for(Transaction testedTransaction: currentMiningBlock.getTransactions()){
                                //TODO check for equal transactions
                                //TODO what if i found one
                            }
                        }
                    }else {
                        boolean success = this.ledger.addBlock(currentMiningBlock);
                        if(success){
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
