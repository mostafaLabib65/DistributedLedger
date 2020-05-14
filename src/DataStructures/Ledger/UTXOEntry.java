package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionOutput;

import java.io.Serializable;

public class UTXOEntry implements Serializable {
    public int blockIndex;
    public int transactionIndex;
    public Transaction transaction;

    public int outputIndex;
    public TransactionOutput transactionOutput;
}
