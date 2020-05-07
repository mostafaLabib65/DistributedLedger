package DataStructures.Block;

import DataStructures.Ledger.UTXOEntry;
import DataStructures.Ledger.UTXOSet;
import DataStructures.Transaction.*;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Block {

    public BlockHeader header;
    public Transaction[] transactions;

    public Block(int N) {
        transactions = new Transaction[N];
        header = new BlockHeader();
    }

    public void setHashOfPreviousBlock(byte[] hash) {
        header.hashOfPrevBlock = hash;
    }

    public  byte[] getMerkleTreeRoot() {
        //TODO !!!
        return null;
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
                entry.transactionOutput = this.transactions[i].transactionOutputs[j];
                utxoSet.addUTXOEntry(key, entry);
            }

        }
    }

    public byte[] getHash() throws NoSuchAlgorithmException {
        return header.hashBlockHeader();
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


    public static void main(String[] args) throws NoSuchAlgorithmException {


        Block b1 = new Block(3);
        Block b2 = new Block(3);

        TransactionInput i1 =  new TransactionInput();
        TransactionInput i2 =  new TransactionInput();


        TransactionOutput o1 = new TransactionOutput();
        TransactionOutput o2 = new TransactionOutput();
        TransactionOutput o3 = new TransactionOutput();
        TransactionOutput o4 = new TransactionOutput();
        TransactionOutput o5 = new TransactionOutput();
        TransactionOutput o6 = new TransactionOutput();
        TransactionOutput o7 = new TransactionOutput();
        TransactionOutput o8 = new TransactionOutput();
        TransactionOutput o9 = new TransactionOutput();


        RSA rsa1 = new RSA(2048);
        RSA rsa2 = new RSA(2048);

        BigInteger pk1 = rsa1.getPublicKey();
        BigInteger pk1mod = rsa1.getModulus();

        BigInteger pk2 = rsa2.getPublicKey();
        BigInteger pk2mod = rsa2.getModulus();


        ArrayList<byte[]> tmp = new ArrayList<>();
        tmp.add(pk1.toByteArray());
        tmp.add(pk1mod.toByteArray());
        byte[] pk1Hash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));

        tmp = new ArrayList<>();
        tmp.add(pk2.toByteArray());
        tmp.add(pk2mod.toByteArray());
        byte[] pk2Hash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));


        o1.amount = 5000;
        o1.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o2.amount = 5000;
        o2.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o3.amount = 5000;
        o3.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o6.amount = 5000;
        o6.publicKeyHash = pk1Hash;

        o7.amount = 7000;
        o7.publicKeyHash = pk1Hash;

        o8.amount = 10000;
        o8.publicKeyHash = pk2Hash;

        o9.amount = 7000;
        o9.publicKeyHash = pk2Hash;

        Transaction t0 = new SpecialTransaction(4);
        t0.transactionOutputs = new TransactionOutput[]{o6, o7 , o8, o9};
        byte[] t0Hash = t0.getTransactionHash();

        i1.publicKey = pk1;
        i1.publicKeyModulus = pk1mod;
        i1.outputIndex = 0;
        BigInteger tmpI = new BigInteger(1, t0Hash);
        i1.signature = rsa1.decrypt(tmpI);
        i1.transactionHash = t0Hash;


        i2.publicKey = pk2;
        i2.publicKeyModulus = pk2mod;
        i2.outputIndex = 2;
        i2.transactionHash = t0Hash;
        i2.signature = rsa2.decrypt(new BigInteger(1, t0Hash));



        Transaction t1 = new NormalTransaction(2,3);

        t1.transactionInputs = new TransactionInput[]{i1, i2};
        t1.transactionOutputs = new TransactionOutput[]{o1, o2, o3};


        b1.header.hashOfPrevBlock = "test".getBytes();
        b1.header.hashOfMerkleRoot = "merkle".getBytes();
        b1.header.nonce = 1;
        b1.transactions = new Transaction[]{t0};

        b2.header.hashOfPrevBlock = "test".getBytes();
        b2.header.hashOfMerkleRoot = "merkle".getBytes();
        b2.header.nonce = 1;
        b2.transactions = new Transaction[]{t1};

        UTXOSet set = new UTXOSet();
        b1.addTransactionsToUTXOSet(set,0);

        System.out.println(b2.isValidPOWBlock(0,set));
        ArrayList<String> t = b2.getUsedUTXOs();


        System.out.println("test");
    }


}
