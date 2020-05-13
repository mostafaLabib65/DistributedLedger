package DataStructures.Ledger;

import DataStructures.Transaction.Transaction;
import Utils.BytesConverter;
import Utils.SHA;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import java.security.NoSuchAlgorithmException;

public class UTXOSet implements Cloneable, Serializable {


    private HashMap<String, UTXOEntry> transactionHashToBlockAndTxIndex;
    private HashMap<String, ArrayList<UTXOEntry>> availableUTXOsForPublicKey;


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
        if (!availableUTXOsForPublicKey.containsKey(publicKeyHash))
            availableUTXOsForPublicKey.put(publicKeyHash, new ArrayList<>());
        availableUTXOsForPublicKey.get(publicKeyHash).add(entry);

    }

    public void removeUTXOEntry(String hash) {

        UTXOEntry entry = transactionHashToBlockAndTxIndex.get(hash);
        String publicKeyHash = BytesConverter.byteToHexString(entry.transactionOutput.publicKeyHash, 64);
        ArrayList<UTXOEntry> tmp = availableUTXOsForPublicKey.get(publicKeyHash);
        if (tmp.contains(entry))
            tmp.remove(entry);
        if (tmp.isEmpty())
            availableUTXOsForPublicKey.remove(publicKeyHash);

        transactionHashToBlockAndTxIndex.remove(hash);
    }


    public ArrayList<UTXOEntry> getUTXOsAvailableForPublicKey(String hash) {
        //TODO handle exception
        //TODO handle no UTXO available (size = 0)
        return availableUTXOsForPublicKey.get(hash);
    }


    public void addTransactionsToUTXOSet(Transaction[] transactions, int blockIndex) throws NoSuchAlgorithmException {
        for (int i = 0; i < transactions.length; i++) {
            byte[] hash = transactions[i].getTransactionHash();

            for (int j = 0; j < transactions[i].getTransactionOutputs().length; j++) {
                ArrayList<byte[]> hashAndIndex = new ArrayList<>();
                byte[] index = BytesConverter.intToBytes(j);
                hashAndIndex.add(hash);
                hashAndIndex.add(index);
                byte[] keyBytes = BytesConverter.concatenateByteArrays(hashAndIndex);
                String key = BytesConverter.byteToHexString(SHA.getSHA(keyBytes), 64);
                UTXOEntry entry = new UTXOEntry();
                entry.transaction = transactions[i];
                entry.blockIndex = blockIndex;
                entry.transactionIndex = i;

                entry.transactionOutput = transactions[i].getTransactionOutputs()[j];
                entry.outputIndex = j;
                this.addUTXOEntry(key, entry);
            }
        }
    }

    public UTXOSet clone() throws CloneNotSupportedException {
        UTXOSet utxoSet = (UTXOSet) super.clone();
        utxoSet.transactionHashToBlockAndTxIndex = (HashMap<String, UTXOEntry>)
                this.transactionHashToBlockAndTxIndex.clone();
        utxoSet.availableUTXOsForPublicKey = (HashMap<String, ArrayList<UTXOEntry>>)
                this.availableUTXOsForPublicKey.clone();
        return utxoSet;
    }


}
