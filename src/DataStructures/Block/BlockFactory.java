package DataStructures.Block;

import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionFactory;
import Utils.BytesConverter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BlockFactory {


    public Block createGenesisBlock(List<String> publicKeyHashes) throws NoSuchAlgorithmException {

        long amount = 1000000000L;
        Block genesisBlock = new Block(publicKeyHashes.size());
        ArrayList<Transaction> transactions = new ArrayList<>();

        TransactionFactory factory = new TransactionFactory();
        for (String publicKeyHash : publicKeyHashes) {
            for(int i = 0; i < 1000; i++){
                Transaction t = factory.createSpecialTransactionForPublicKey(
                        publicKeyHash.getBytes(), amount);
                transactions.add(t);
            }
        }

        BlockHeader header = new BlockHeader();
        genesisBlock.setTransactions(transactions.toArray(new Transaction[0]));

        header.nonce = -1;
        header.hashOfPrevBlock = new byte[32];
        header.hashOfMerkleRoot = genesisBlock.getMerkleTreeRoot();
        genesisBlock.setHeader(header);

        return genesisBlock;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {

        List<String> pks = new ArrayList<>();


        pks.add("ya 7osty");
        pks.add("el soda");
        pks.add("yana yama");

        BlockFactory factory = new BlockFactory();

        Block b = factory.createGenesisBlock(pks);

        System.out.println("test");






    }
}
