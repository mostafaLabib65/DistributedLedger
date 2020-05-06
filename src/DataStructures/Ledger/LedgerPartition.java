package DataStructures.Ledger;

import DataStructures.Block.Block;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionOutput;
import Utils.BytesConverter;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class LedgerPartition {

    ArrayList<Block> blocks;
    UTXOSet utxoSet;
    int currentDifficulty;
    int startingLedgerIndex;

    boolean addBlock(Block b) throws NoSuchAlgorithmException {
        if(!(
                BytesConverter.byteToHexString(b.getPreviousHash(), 64).equals(
                BytesConverter.byteToHexString(blocks.get(blocks.size() - 1).getPreviousHash(), 64)
        ))) return false;

        if(!b.isValidPOWBlock(currentDifficulty, utxoSet)) return false;

        blocks.add(b);
        b.addTransactionsToUTXOSet(utxoSet, startingLedgerIndex + blocks.size());
        ArrayList<String> usedUTXOs = b.getUsedUTXOs();
        for (int i = 0; i < usedUTXOs.size(); i++) {

            //TODO ORDER IS IMPORTANT!!

            utxoSet.removeUTXOToPublicKey(usedUTXOs.get(i));
            utxoSet.removeUTXOEntry(usedUTXOs.get(i));
        }

        return true;
    }

    public int getSize(){
        return blocks.size();
    }



}
