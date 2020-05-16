package Nodes.MinerUtils;

import DataStructures.Block.Block;
import DataStructures.Block.BlockFactory;
import DataStructures.Block.BlockHeader;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionFactory;
import Utils.RSA;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean leader;
    private int numOfParticipants;
    private List<String> hashedPublicKeys;
    private Ledger ledger;
    private Process process;
    private Transaction[] blockTransactions;
    private ReentrantLock readyToMineBlocksQueueLock;
    public  BlockProducer(ArrayList<Block> readyToMineBlocks, ArrayList<Transaction> transactions,
                          int blockSize, RSA rsa, boolean leader, Thread blockConsumer, List<String> hashedPublicKeys,
                          int numOfParticipants, Ledger ledger, Process process, ReentrantLock readyToMineBlocksQueueLock){
        this.readyToMineBlocks = readyToMineBlocks;
        this.transactions = transactions;
        this.blockSize = blockSize;
        this.rsa = rsa;
        this.blockConsumer = blockConsumer;
        this.leader = leader;
        this.numOfParticipants = numOfParticipants;
        this.hashedPublicKeys = hashedPublicKeys;
        this.ledger = ledger;
        this.process = process;
        this.blockTransactions = new Transaction[blockSize];
        this.readyToMineBlocksQueueLock = readyToMineBlocksQueueLock;
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
    protected void sendLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_LEDGER);
        cu.setLedger(ledger);
        process.invokeClientEvent(cu);
    }
    @Override
    public void run() {
        synchronized (this){
            if(leader){
                BlockFactory factory = new BlockFactory();
                try {
                    if (hashedPublicKeys.size() != numOfParticipants){
                        System.out.println("Block producer: Waiting for hash keys");
                        wait();
                    }
                } catch (InterruptedException e){
                    System.out.println("Block producer: Hash keys received");
                    try {
                        System.out.println("Block Producer: Sending Ledger");
                        this.genesisBlock = factory.createGenesisBlock(hashedPublicKeys);
                        boolean success = this.ledger.addBlock(this.genesisBlock);
                        this.sendLedger();
                    } catch (NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    } catch (Exception exception) {
                        System.out.println(exception.getMessage());
                    }
                }
            }
            while (true){
                while (transactions.size() == 0) {
                    try {
                        System.out.println("Block producer: Waiting for transactions");
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("Block producer: transaction received start working....");
                        this.initializeBlock();
                    }
                }
                Transaction temp = this.transactions.get(0);
                this.transactions.remove(temp);
                this.blockTransactions[this.transactionCounter] = temp;
                this.transactionCounter++;
                if(this.transactionCounter == blockSize-1){
                    try {
                        Transaction t = f.createRewardTransactionForPublicKey(rsa.getPublicKey(), rsa.getModulus());
                        this.blockTransactions[this.transactionCounter] = t;
                        this.block.setTransactions(blockTransactions);
                        this.blockTransactions = new Transaction[blockSize];
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    this.transactionCounter = 0;
                    try {
                      this.block.getHeader().hashOfMerkleRoot = this.block.getMerkleTreeRoot();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    readyToMineBlocksQueueLock.lock();
                    this.readyToMineBlocks.add(this.block);
                    if(readyToMineBlocks.size() == 1){
                        blockConsumer.interrupt();
                    }
                    readyToMineBlocksQueueLock.unlock();
                    this.initializeBlock();
                }
                if(this.interrupted){
                    readyToMineBlocksQueueLock.lock();
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
                    readyToMineBlocksQueueLock.unlock();
                    this.interrupted = false;
                }
            }
        }
    }
}
