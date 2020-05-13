package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Ledger.UTXOSet;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class BlockNode implements Serializable {


    private UTXOSet utxoSet;
    private Block block;
    private BlockNode parent;
    private List<BlockNode> children;
    private int height;

    public BlockNode() {
        this.height = 0;
        this.children = new ArrayList<>();
    }

    public BlockNode(Block block) throws NoSuchAlgorithmException {
        this.block = block;
        this.height = 0;
        this.children = new ArrayList<>();
        this.utxoSet = new UTXOSet();
        this.utxoSet.addTransactionsToUTXOSet(this.block.getTransactions(), 0);
    }

    public BlockNode(Block block, BlockNode parent) throws CloneNotSupportedException, NoSuchAlgorithmException {
        this.block = block;
        this.parent = parent;
        this.height = parent.height + 1;
        this.children = new ArrayList<>();
        this.utxoSet = parent.getUtxoSet().clone();
        this.utxoSet.addTransactionsToUTXOSet(this.block.getTransactions(), height);
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public BlockNode getParent() {
        return parent;
    }

    public void setParent(BlockNode parent) {
        this.parent = parent;
    }

    public List<BlockNode> getChildren() {
        return children;
    }

    public void setChildren(List<BlockNode> children) {
        this.children = children;
    }

    public UTXOSet getUtxoSet() {
        return utxoSet;
    }

    public void setUtxoSet(UTXOSet utxoSet) {
        this.utxoSet = utxoSet;
    }

    public BlockNode addNode(Block block) throws NoSuchAlgorithmException, CloneNotSupportedException {
        BlockNode node = new BlockNode(block, this);
        this.children.add(node);
        return node;
    }

    public boolean equals(BlockNode node) {
        try {
            return this.block.getMerkleTreeRoot().equals(node.getBlock().getMerkleTreeRoot());
        } catch (Exception e) {
            return false;
        }
    }
}
