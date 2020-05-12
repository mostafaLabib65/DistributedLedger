package DataStructures.Block;

import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionFactory;
import Utils.BytesConverter;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BlockFactory {


    public Block createGensisBlock(ArrayList<byte[]> publicKeyHashes, ArrayList<Long> startingAmount) throws NoSuchAlgorithmException {

        Block gensisBlock = new Block(startingAmount.size());
        ArrayList<Transaction> transactions = new ArrayList<>();

        TransactionFactory factory = new TransactionFactory();
        for (int i = 0; i < startingAmount.size(); i++) {
            Transaction t = factory.createSpecialTransactionForPublicKey(publicKeyHashes.get(i), startingAmount.get(i));
            transactions.add(t);
        }

        BlockHeader header = new BlockHeader();
        gensisBlock.setTransactions(transactions.toArray(new Transaction[0]));

        header.nonce = -1;
        header.hashOfPrevBlock = new byte[32];
        header.hashOfMerkleRoot = gensisBlock.getMerkleTreeRoot();
        gensisBlock.setHeader(header);

        return gensisBlock;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {

        ArrayList<byte[]> pks = new ArrayList<>();
        ArrayList<Long> am = new ArrayList<>();


        pks.add("ya 7osty".getBytes());
        pks.add("el soda".getBytes());
        pks.add("yana yama".getBytes());

        am.add(new Long(20000));
        am.add(new Long(40000));
        am.add(new Long(60000));


        BlockFactory factory = new BlockFactory();

        Block b = factory.createGensisBlock(pks, am);

        System.out.println("test");




    }
}
