package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Ledger.UTXOEntry;

import java.io.Serializable;
import java.util.HashMap;

public class TransientPartitionTree implements Serializable {



    private HashMap<String, BlockNode> hashOfLastBlockinPartition; //TODO DONT FORGET TO UPDATE
    private int currentMaxDepth;

    public TransientPartitionTree() {
        hashOfLastBlockinPartition = new HashMap<>();
        currentMaxDepth = 0;
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
