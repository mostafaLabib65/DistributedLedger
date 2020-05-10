package Nodes;

import DataStructures.Block.Block;
import DataStructures.Block.BlockHeader;
import DataStructures.Transaction.Transaction;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BlockProducer implements Runnable {

    private ArrayList<Block> readyToMineBlocks;
    private ArrayList<Transaction> transactions;
    private Block block;
    private int blockSize;
    private int transactionCounter = 0;
    private boolean interrupted = false;
    private Block receivedBlock;
    public  BlockProducer(ArrayList<Block> readyToMineBlocks, ArrayList<Transaction> transactions, int blockSize){
        this.readyToMineBlocks = readyToMineBlocks;
        this.transactions = transactions;
        this.blockSize = blockSize;
    }

    private void initializeBlock(){
        BlockHeader header = new BlockHeader();
        header.nonce = -1;
        this.block = new Block(this.blockSize);
        block.setHeader(header);
    }
    public void setInterrupt(Block receivedBlock){
        this.interrupted = true;
        this.receivedBlock = receivedBlock;
    }

    @Override
    public void run() {
        while (true){
            synchronized (this){
                while (transactions.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Transaction temp = this.transactions.get(0);
                this.transactions.remove(temp);
                this.block.getTransactions()[this.transactionCounter] = temp;
                this.transactionCounter++;
                if(this.transactionCounter == blockSize-1){
                    this.transactionCounter = 0;
                    try {
                        this.block.getMerkleTreeRoot();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    this.readyToMineBlocks.add(this.block);
                    notify();
                    this.initializeBlock();
                }
                if(this.interrupted){
                    for(Transaction t: receivedBlock.getTransactions()){
                        //TODO check if transaction repeated in any of the blocks
                        //TODO what if one is found
                    }
                    this.interrupted = false;
                }
            }
        }
    }
}
