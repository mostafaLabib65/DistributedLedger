package DataStructures.Block.MerkleTree;

import Utils.SHA;

import java.security.NoSuchAlgorithmException;

public class MerkleNode {
    private byte[] hash;
    private MerkleNode leftChild;
    private MerkleNode rightChild;

    public MerkleNode() {
    }

    public MerkleNode(byte[] hash) {
        this.hash = hash;
    }

    public MerkleNode(MerkleNode leftChild, MerkleNode rightChild) throws NoSuchAlgorithmException {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.computeHash();
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public void setLeftChild(MerkleNode leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(MerkleNode rightChild) {
        this.rightChild = rightChild;
    }

    private void computeHash() throws NoSuchAlgorithmException {
        byte[] leftHash = this.leftChild.getHash(),
                rightHash = this.rightChild.getHash();

        byte[] childrenBlocks = new byte[leftHash.length + rightHash.length];
        System.arraycopy(leftHash, 0, childrenBlocks, 0, leftHash.length);
        System.arraycopy(rightHash, 0, childrenBlocks, leftHash.length, rightHash.length);
        this.setHash(SHA.getSHA(childrenBlocks));
    }
}
