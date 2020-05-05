package DataStructures.Block;

import DataStructures.Ledger.UTXOEntry;
import DataStructures.Ledger.UTXOSet;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionInput;
import DataStructures.Transaction.TransactionOutput;
import DataStructures.Transaction.TransactionsValidator;
import Utils.BytesConverter;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Block {

    public BlockHeader header;
    public Transaction[] transactions;

    public void setMerkleTree() {
        //TODO !!!
    }


    public byte[] getPreviousHash() {
        return header.hashOfPrevBlock;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }

    public boolean isValidPOWBlock(int difficulty, UTXOSet utxoSet) throws NoSuchAlgorithmException {

        BigInteger hash = new BigInteger(1, this.header.hashBlockHeader());
        BigInteger b1 = new BigInteger("2");
        int exponent = 256 - difficulty;
        BigInteger limit = b1.pow(exponent);
        if(hash.compareTo(limit) >= 0) return false;
        return TransactionsValidator.validateSetOfTransactions(this.transactions,utxoSet);

    }

    public void addTransactionsToUTXOSet(UTXOSet utxoSet, int blockIndex) throws NoSuchAlgorithmException {

        for (int i = 0; i < this.transactions.length; i++) {
            byte[] hash = transactions[i].getTransactionHash();

            for (int j = 0; j < this.transactions[i].transactionOutputs.length; j++) {

                ArrayList<byte[]> hashAndIndex = new ArrayList<>();
                byte[] index = BytesConverter.intToBytes(j);
                hashAndIndex.add(hash);
                hashAndIndex.add(index);
                byte[] keyBytes = BytesConverter.concatenateByteArrays(hashAndIndex);

                String key = BytesConverter.byteToHexString(SHA.getSHA(keyBytes),64);

                UTXOEntry entry = new UTXOEntry();

                entry.transaction = this.transactions[i];
                entry.blockIndex = blockIndex;
                entry.transactionIndex = i;

                utxoSet.add(key, entry);

            }

        }
    }

    public ArrayList<String> getUsedUTXOs() throws NoSuchAlgorithmException {
        ArrayList<String> usedUTXOs = new ArrayList<>();
        for (int i = 0; i < this.transactions.length; i++) {
            for (int j = 0; j < this.transactions[i].transactionInputs.length; j++) {

                TransactionInput input = transactions[i].getTransactionInputs()[j];
                byte[] hash = input.transactionHash;
                ArrayList<byte[]> hashAndIndex = new ArrayList<>();
                byte[] index = BytesConverter.intToBytes(input.outputIndex);
                hashAndIndex.add(hash);
                hashAndIndex.add(index);
                byte[] keyBytes = BytesConverter.concatenateByteArrays(hashAndIndex);

                String key = BytesConverter.byteToHexString(SHA.getSHA(keyBytes),64);

                usedUTXOs.add(key);

            }

        }
        return usedUTXOs;
    }
}
