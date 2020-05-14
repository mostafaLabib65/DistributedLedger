package DataStructures.Ledger;

import DataStructures.Block.Block;
import DataStructures.Ledger.PartitionsTree.TransientPartitionTree;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

public class Ledger implements Serializable {


    private LedgerPartition baseLedger;
    private TransientPartitionTree partitionTree;
    private int transientLedgerMaxLength;

    public Ledger() {
        baseLedger = new LedgerPartition(0);
        partitionTree = new TransientPartitionTree();
        transientLedgerMaxLength = 10;
    }

    public boolean addBlock(Block block) throws NoSuchAlgorithmException {
        if (!partitionTree.addBlock(block))
            return false;

        if (partitionTree.getMaxBranchDepth() > transientLedgerMaxLength) {
            Block tmp = partitionTree.removeFirstBlock();
            baseLedger.addBlock(tmp);
        }

        return true;
    }

    public UTXOEntry[] getAvailableUTXOsForPublicKey(String publicKeyHash) {
        //TODO check validity
        return partitionTree.getLongestBranchUTXOSet(publicKeyHash);
    }


    public int getLedgerDepth() {
        //TODO Change
        return baseLedger.getDepth() + partitionTree.getMaxBranchDepth();
    }

    public byte[] getLastBlockHash() throws NoSuchAlgorithmException {
        return baseLedger.getLastBlockHash();
    }


    public boolean isValidBlockForLedger(Block b) throws NoSuchAlgorithmException {
        return this.baseLedger.isValidBlockForLedger(b);
    }
}
