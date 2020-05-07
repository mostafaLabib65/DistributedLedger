package DataStructures.Ledger;

import DataStructures.Block.Block;
import DataStructures.Block.BlockHeader;
import DataStructures.Transaction.*;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class LedgerPartition {

    ArrayList<Block> blocks;
    UTXOSet utxoSet;
    int currentDifficulty;
    int startingLedgerIndex;


    public LedgerPartition(int start) {

        blocks = new ArrayList<>();
        startingLedgerIndex = start;
        utxoSet = new UTXOSet();

        //TODO set difficulty
        currentDifficulty = 0;
    }



    public boolean addBlock(Block b) throws NoSuchAlgorithmException {

        if(!blocks.isEmpty())
            if(!(
                    BytesConverter.byteToHexString(b.getPreviousHash(), 64).equals(
                    BytesConverter.byteToHexString(blocks.get(blocks.size() - 1).getHash(), 64)
            ))) return false;

        if(!b.isValidPOWBlock(currentDifficulty, utxoSet)) return false;

        blocks.add(b);
        b.addTransactionsToUTXOSet(utxoSet, startingLedgerIndex + blocks.size() - 1);
        ArrayList<String> usedUTXOs = b.getUsedUTXOs();
        for (int i = 0; i < usedUTXOs.size(); i++) {
            utxoSet.removeUTXOEntry(usedUTXOs.get(i));
        }

        return true;
    }

    public int getSize(){
        return blocks.size();
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
        o3.publicKeyHash = pk1Hash;

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
        b2.setHashOfPreviousBlock(b1.getHash());



        LedgerPartition partition = new LedgerPartition(0);


        TransactionInput i3 = new TransactionInput();
        Transaction t2 = new NormalTransaction(1,1);


        i3.publicKey = pk1;
        i3.publicKeyModulus = pk1mod;
        i3.outputIndex = 2;
        tmpI = new BigInteger(1, t1.getTransactionHash());
        i3.signature = rsa1.decrypt(tmpI);
        i3.transactionHash = t1.getTransactionHash();


        t2.transactionInputs = new TransactionInput[]{i3};
        t2.transactionOutputs = new TransactionOutput[]{o1};

        Block b3 = new Block(1);

        b3.header.hashOfPrevBlock = "test".getBytes();
        b3.header.hashOfMerkleRoot = "merkle".getBytes();
        b3.header.nonce = 1;
        b3.transactions = new Transaction[]{t2};
        b3.setHashOfPreviousBlock(b1.getHash());


        Block b4 = new Block(1);

        b4.header.hashOfPrevBlock = "test".getBytes();
        b4.header.hashOfMerkleRoot = "merkle".getBytes();
        b4.header.nonce = 1;
        b4.transactions = new Transaction[]{t0};
        b4.setHashOfPreviousBlock(b2.getHash());




        System.out.println(partition.addBlock(b1));
        System.out.println(partition.addBlock(b2));
        System.out.println(partition.addBlock(b3));
        System.out.println(partition.addBlock(b4));

        System.out.println("test");

    }


}
