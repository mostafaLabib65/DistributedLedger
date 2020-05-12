package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Block.BlockHeader;
import DataStructures.Ledger.UTXOEntry;
import Utils.BytesConverter;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class TransientPartitionTree implements Serializable {

    private HashMap<String, BlockNode> nodes; //TODO DONT FORGET TO UPDATE
    private int currentMaxDepth;
    private BlockNode root;
    private BlockNode longestLeaf;

    public TransientPartitionTree() {
        nodes = new HashMap<>();
        currentMaxDepth = 0;
    }


    public Block removeFirstBlock() {
        Block rootBlock = root.getBlock();
        root = this.getNextCandidateNodeFromRoot(root, longestLeaf);
        return rootBlock;
    }


    public boolean addBlock(Block block) {
        try {
            String parentHash = BytesConverter.byteToHexString(block.getHeader().getHashOfPrevBlock(), 64);
            String nodeHash = BytesConverter.byteToHexString(block.getMerkleTreeRoot(), 64);
            if (root == null) {
                root = new BlockNode(block);
                longestLeaf = root;
                nodes.put(nodeHash, root);
                return true;
            } else {
                if (nodes.containsKey(parentHash)) {
                    BlockNode parent = nodes.get(parentHash);
                    BlockNode node = parent.addNode(block);
                    nodes.put(nodeHash, node);
                    updateLongestLeaf(node);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }


    public int getMaxBranchDepth() {
        return longestLeaf == null ? 0 : longestLeaf.getHeight() - root.getHeight();
    }

    public UTXOEntry[] getLongestBranchUTXOSet() {
        //TODO

        return null;
    }

    private void updateLongestLeaf(BlockNode node) {
        this.longestLeaf = node.getHeight() > longestLeaf.getHeight() ? node : longestLeaf;
    }

    /**
     * Helper function intended to help in advancing in transient ledger tree
     * P.S., It takes linear time in worst case scenario, O(h) time
     *
     * @param root root node of the transient ledger tree
     * @param leaf longest leaf from the root
     * @return second node from the root in the path between root and leaf
     */
    private BlockNode getNextCandidateNodeFromRoot(BlockNode root, BlockNode leaf) {
        BlockNode currentNode = leaf;
        while (!currentNode.getParent().equals(root)) {
            currentNode = currentNode.getParent();
        }
        return currentNode;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        TransientPartitionTree tree = new TransientPartitionTree();
        // Block 1
        Block b1 = new Block(0);
        b1.hash = new byte[1];
        b1.hash[0] = 0;
        BlockHeader h1 = new BlockHeader();
        h1.hashOfPrevBlock = new byte[1];
        h1.hashOfPrevBlock[0] = 1;
        b1.setHeader(h1);

        tree.addBlock(b1);

        // Block 2
        Block b2 = new Block(0);
        b2.hash = new byte[1];
        b2.hash[0] = 1;
        BlockHeader h2 = new BlockHeader();
        h2.hashOfPrevBlock = new byte[1];
        h2.hashOfPrevBlock[0] = 0;
        b2.setHeader(h2);

        tree.addBlock(b2);

        // Block 3
        Block b3 = new Block(0);
        b3.hash = new byte[1];
        b3.hash[0] = 2;
        BlockHeader h3 = new BlockHeader();
        h3.hashOfPrevBlock = new byte[1];
        h3.hashOfPrevBlock[0] = 0;
        b3.setHeader(h3);

        tree.addBlock(b3);

        // Block 4
        Block b4 = new Block(0);
        b4.hash = new byte[1];
        b4.hash[0] = 3;
        BlockHeader h4 = new BlockHeader();
        h4.hashOfPrevBlock = new byte[1];
        h4.hashOfPrevBlock[0] = 1;
        b4.setHeader(h4);

        tree.addBlock(b4);

        System.out.println("ledger length: " + tree.getMaxBranchDepth());

        tree.removeFirstBlock();

        System.out.println("ledger length: " + tree.getMaxBranchDepth());
    }

}
