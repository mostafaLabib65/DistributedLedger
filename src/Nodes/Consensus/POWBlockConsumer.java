package Nodes.Consensus;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.Process;
import network.entities.CommunicationUnit;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
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
    private boolean waitingForBlocks = true;
    private ArrayList<Transaction> allTransactions;
    private ReentrantLock readyToMineBlocksQueueLock;
    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger, ArrayList<Transaction> transactions,
                          ReentrantLock readyToMineBlocksQueueLock){
        this.blocks = blocks;
        this.process = process;
        this.ledger = ledger;
        this.allTransactions = transactions;
        cu.setEvent(BLOCK);
        this.readyToMineBlocksQueueLock = readyToMineBlocksQueueLock;
    }

    public void setLedger(Ledger ledger) {
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

    private void waitForBlocks(){
        try {
            System.out.println("POW Consumer: Waiting for new blocks");
            waitingForBlocks = true;
            wait();
        } catch (InterruptedException e) {
            System.out.println("POW Consumer: Block received start working....");
        }
    }

    private void getNewBlock(){
        waitingForBlocks = false;
        readyToMineBlocksQueueLock.lock();
        this.currentMiningBlock = this.blocks.get(0);
        this.blocks.remove(currentMiningBlock);
        readyToMineBlocksQueueLock.unlock();
        try {
            currentMiningBlock.getHeader().hashOfPrevBlock = this.ledger.getLastBlockHash();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        nonce = 0;
    }

    private void startMining(){
        System.out.println("POW Consumer: Start Mining....");
        try {
            do{
                currentMiningBlock.getHeader().nonce = nonce;
                nonce++;
            }while (!isValidPOWBlock(currentMiningBlock, this.difficulty) && !interrupt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void checkForDuplicateTransactions(){
        System.out.println("POW Consumer: Checking for repeated transactions....");

        Transaction[] transactions = currentMiningBlock.getTransactions();
        for(Transaction acceptedTransaction: receivedBlock.getTransactions()){
            int index = 0;
            try {
                index = currentMiningBlock.getIndexOfTransaction(acceptedTransaction);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
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
    }
    @Override
    public void run() {
        while (true){
            synchronized (this){
                readyToMineBlocksQueueLock.lock();
                int size = blocks.size();
                readyToMineBlocksQueueLock.unlock();
                if(size == 0 ){
                    waitForBlocks();
                }
                readyToMineBlocksQueueLock.lock();
                size = blocks.size();
                readyToMineBlocksQueueLock.unlock();
                if(size != 0){
                    getNewBlock();
                }
                this.interrupt = false;
                this.blockCorrupted = false;
                startMining();
                if(interrupt){
                    checkForDuplicateTransactions();
                }else {
                    boolean success = false;
                    try {
                        success = this.ledger.addBlock(currentMiningBlock);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("POW Consumer: 148" + e.getMessage());
                    }
                    if(success){
                        System.out.println("POW Consumer: block added to ledger, start publishing it...");
                        cu.setBlock(currentMiningBlock);
                        this.process.invokeClientEvent(cu);
                        System.out.println("POW Consumer.............................." + currentTimeMillis() + " Ledger length: " + ledger.getLedgerDepth());
                    }else {
                        System.out.println("POW Consumer: Failed to add block to ledger...");

                    }
                }

            }
        }
    }
}
