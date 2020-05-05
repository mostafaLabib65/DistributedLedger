package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;

public class UTXOEntry {
    public int blockIndex;
    public int transactionIndex;
    public Transaction transaction;
}
