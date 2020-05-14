package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Ledger.UTXOSet;

import java.io.Serializable;

public class BlockNode implements Serializable {

    UTXOSet utxoSet;
    Block block;
}
