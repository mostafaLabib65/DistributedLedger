package DataStructures.Ledger;

import DataStructures.Block.Block;
import DataStructures.Ledger.PartitionsTree.TransientPartitionTree;

import java.security.NoSuchAlgorithmException;

public class Ledger {

    LedgerPartition baseLeadger;
    TransientPartitionTree partitionTree;
    int transientLedgerMaxLength;

    public Ledger() {
        baseLeadger = new LedgerPartition(0);
        partitionTree = new TransientPartitionTree();
        transientLedgerMaxLength = 0;
    }

    public boolean addBlock(Block block) throws NoSuchAlgorithmException {
        if(!partitionTree.addBlock(block))
            return false;
        if (partitionTree.getMaxBranchDepth() > transientLedgerMaxLength) {
            Block tmp = partitionTree.removeFirstBlock();
            baseLeadger.addBlock(tmp);
        }
        return true;
    }

    public UTXOEntry[] getAvailableUTXOsForPublicKey(String publicKeyHash) {

        //TODO check validity
        return partitionTree.getLongestBranchUTXOSet();
    }

}
