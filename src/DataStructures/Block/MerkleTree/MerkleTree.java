package DataStructures.Block.MerkleTree;

import DataStructures.Transaction.Transaction;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
    private MerkleNode root;
    private List<MerkleNode> leaves;

    public MerkleTree() {
        this.root = new MerkleNode();
        this.leaves = new ArrayList<>();
    }

    public MerkleNode getRoot() {
        return root;
    }

    public byte[] getRootHash() {
        return root.getHash();
    }

    public void addTransaction(Transaction transaction) throws NoSuchAlgorithmException {
        this.leaves.add(new MerkleNode(transaction.getTransactionHash()));
    }

    public void addTransactions(Transaction[] transactions) throws NoSuchAlgorithmException {
        for (Transaction transaction : transactions) {
            this.leaves.add(new MerkleNode(transaction.getTransactionHash()));
        }
    }

    public void buildTree() throws NoSuchAlgorithmException {
        this.buildTree(this.leaves);
    }

    private void buildTree(List<MerkleNode> nodes) throws NoSuchAlgorithmException {
        if (nodes.size() == 0)
            throw new InvalidParameterException("");

        if (nodes.size() == 1) {
            this.root = nodes.get(0);
            return;
        }

        List<MerkleNode> parents = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i += 2) {
            MerkleNode leftChild = nodes.get(i);
            MerkleNode rightChild = nodes.get(i);
            if (i + 1 < nodes.size())
                rightChild = nodes.get(i + 1);
            parents.add(new MerkleNode(leftChild, rightChild));
        }

    }

}
