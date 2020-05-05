package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;

import java.util.HashMap;

public class UTXOSet {

    private HashMap<String,UTXOEntry> transactionHashToBlockAndTxIndex;

    public UTXOSet() {
        transactionHashToBlockAndTxIndex = new HashMap<>();
    }

    public boolean contains(String hash) {
        return transactionHashToBlockAndTxIndex.containsKey(hash);
    }

    public Transaction getTransaction(String hashString) {
        return transactionHashToBlockAndTxIndex.get(hashString).transaction;

    }

    public void add(String key, UTXOEntry entry) {
        transactionHashToBlockAndTxIndex.put(key, entry);
    }

    public void remove(String hash) {
        transactionHashToBlockAndTxIndex.remove(hash);
    }
}
