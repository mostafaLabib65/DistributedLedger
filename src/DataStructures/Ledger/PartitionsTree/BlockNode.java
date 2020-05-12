package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Ledger.UTXOSet;

import java.util.List;
import java.io.Serializable;

public class BlockNode implements Serializable {


    private UTXOSet utxoSet;
    private Block block;
    private BlockNode parent;
    private List<BlockNode> children;
    private int maxDepth;

    public BlockNode() {
        maxDepth = 0;
    }
}
