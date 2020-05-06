package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Ledger.LedgerPartition;
import DataStructures.Ledger.UTXOEntry;

import java.util.HashMap;

public class TransientPartitionTree {



    private HashMap<String, LedgerPartition> hashOfLastBlockinPartition; //TODO DONT FORGET TO UPDATE

    public TransientPartitionTree() {
        hashOfLastBlockinPartition = new HashMap<>();
    }


    public Block removeFirstBlock() {

        //TODO
        return null;
    }


    public boolean addBlock(Block block){
        //TODO
        return false;
    }


    public int getMaxBranchDepth(){
        //TODO
        return 0;
    }

    public UTXOEntry[] getLongestBranchUTXOSet() {
        //TODO

        return null;
    }

}
