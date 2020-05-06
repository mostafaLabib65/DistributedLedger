package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionOutput;

public class UTXOEntry {
    public int blockIndex;
    public int transactionIndex;
    public Transaction transaction;
    public TransactionOutput transactionOutput;
}
