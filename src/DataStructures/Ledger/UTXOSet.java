package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionOutput;
import Utils.BytesConverter;

import java.util.ArrayList;
import java.util.HashMap;

public class UTXOSet {

    private HashMap<String,UTXOEntry> transactionHashToBlockAndTxIndex;
    private HashMap<String,ArrayList<UTXOEntry>> availableUTXOsForPublicKey;


    public UTXOSet() {
        transactionHashToBlockAndTxIndex = new HashMap<>();
        availableUTXOsForPublicKey = new HashMap<>();
    }

    public boolean contains(String hash) {
        return transactionHashToBlockAndTxIndex.containsKey(hash);
    }

    public Transaction getTransaction(String hashString) {
        return transactionHashToBlockAndTxIndex.get(hashString).transaction;

    }

    public void addUTXOEntry(String key, UTXOEntry entry) {

        transactionHashToBlockAndTxIndex.put(key, entry);
        String publicKeyHash = BytesConverter.byteToHexString(entry.transactionOutput.publicKeyHash, 64);
        if(!availableUTXOsForPublicKey.containsKey(publicKeyHash))
            availableUTXOsForPublicKey.put(publicKeyHash, new ArrayList<>());
        availableUTXOsForPublicKey.get(publicKeyHash).add(entry);

    }
    public void removeUTXOEntry(String hash) {

        UTXOEntry entry = transactionHashToBlockAndTxIndex.get(hash);
        String publicKeyHash = BytesConverter.byteToHexString(entry.transactionOutput.publicKeyHash, 64);
        ArrayList<UTXOEntry> tmp = availableUTXOsForPublicKey.get(publicKeyHash);
        if(tmp.contains(entry))
            tmp.remove(entry);
        if(tmp.isEmpty())
            availableUTXOsForPublicKey.remove(publicKeyHash);

        transactionHashToBlockAndTxIndex.remove(hash);
    }


    public ArrayList<UTXOEntry> getUTXOsAvailableForPublicKey(String hash) {
        //TODO handle exception
        return availableUTXOsForPublicKey.get(hash);
    }



}
