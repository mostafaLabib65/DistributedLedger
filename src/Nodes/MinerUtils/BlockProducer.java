package Nodes.MinerUtils;

import DataStructures.Block.Block;
import DataStructures.Block.BlockFactory;
import DataStructures.Block.BlockHeader;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionFactory;
import Utils.RSA;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class BlockProducer implements Runnable {

    private ArrayList<Block> readyToMineBlocks;
    private ArrayList<Transaction> transactions;
    private Block block;
    private int blockSize;
    private int transactionCounter = 0;
    private boolean interrupted = false;
    private Block receivedBlock;
    private TransactionFactory f =new TransactionFactory();
    private RSA rsa;
    private Thread blockConsumer;
    private Block genesisBlock;
    public  BlockProducer(ArrayList<Block> readyToMineBlocks, ArrayList<Transaction> transactions, int blockSize, RSA rsa, boolean leader, Thread blockConsumer, List<String> hashedPublicKeys, int numOfParticipants){
        this.readyToMineBlocks = readyToMineBlocks;
        this.transactions = transactions;
        this.blockSize = blockSize;
        this.rsa = rsa;
        this.blockConsumer = blockConsumer;
        if(leader){
            BlockFactory factory = new BlockFactory();
            try {
                while (hashedPublicKeys.size() != numOfParticipants){
                    sleep(2);
                }
                this.genesisBlock = factory.createGenesisBlock(hashedPublicKeys);
            } catch (NoSuchAlgorithmException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Block getGenesisBlock(){
        return this.genesisBlock;
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
                    try {
                        Transaction t = f.createRewardTransactionForPublicKey(rsa.getPublicKey(), rsa.getModulus());
                        this.block.getTransactions()[this.transactionCounter] = t;
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    this.transactionCounter = 0;
                    try {
                        this.block.getMerkleTreeRoot();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    this.readyToMineBlocks.add(this.block);
                    if(readyToMineBlocks.size() == 1){
                        blockConsumer.interrupt();
                    }
                    notify();
                    this.initializeBlock();
                }
                if(this.interrupted){
                    for(Block b: readyToMineBlocks){
                        boolean corrupted = false;
                        for(Transaction t: receivedBlock.getTransactions()){
                            try {
                                int index = b.getIndexOfTransaction(t);
                                if(index != -1){
                                    b.getTransactions()[index] = null;
                                    corrupted = true;
                                }
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                        if(corrupted){
                            for(Transaction t: b.getTransactions()){
                                if(t != null)
                                    this.transactions.add(0, t);
                            }
                            readyToMineBlocks.remove(b);
                        }
                    }
                    this.interrupted = false;
                }
            }
        }
    }
}
