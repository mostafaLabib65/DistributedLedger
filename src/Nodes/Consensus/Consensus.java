package Nodes.Consensus;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import Nodes.MinerUtils.BlockAdder;
import network.Process;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;


public abstract class Consensus implements Runnable {

    @Override
    public void run() {

    }

    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger, ArrayList<Transaction> transactions,
                          ReentrantLock readyToMineBlocksQueueLock) {

    }

    public void setParams(ArrayList<Block> blocks, Process process, Ledger ledger, boolean leader, ArrayList<Block> votingBlocksQueue,
                          Thread votingSystemLeaderThread, ReentrantLock readyToMineBlocksQueueLock) {

    }

    public void StopMiningCurrentBlock(Block block) {
    }

    public void setLedger(Ledger ledger) {

    }

    public void setReceivedBlock(Block receivedBlock) {

    }

    public void setBlockAdder(BlockAdder blockAdder) {

    }
}
