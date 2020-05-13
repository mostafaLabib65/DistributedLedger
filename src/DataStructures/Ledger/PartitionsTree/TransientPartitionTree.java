package DataStructures.Ledger.PartitionsTree;

import DataStructures.Block.Block;
import DataStructures.Block.BlockHeader;
import DataStructures.Ledger.UTXOEntry;
import DataStructures.Transaction.*;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class TransientPartitionTree implements Serializable {

    private HashMap<String, BlockNode> nodes; //TODO DONT FORGET TO DELETE PRUNED NODES
    private BlockNode root;
    private BlockNode longestLeaf;

    public TransientPartitionTree() {
        nodes = new HashMap<>();
    }


    public Block removeFirstBlock() {
        Block rootBlock = root.getBlock();
        root = this.getNextCandidateNodeFromRoot(root, longestLeaf);
        return rootBlock;
    }


    public boolean addBlock(Block block) {
        try {
            String parentHash = BytesConverter.byteToHexString(block.getHeader().getHashOfPrevBlock(), 64);
            String nodeHash = BytesConverter.byteToHexString(block.getMerkleTreeRoot(), 64);
            if (root == null) {
                root = new BlockNode(block);
                longestLeaf = root;
                nodes.put(nodeHash, root);
                return true;
            } else {
                if (nodes.containsKey(parentHash)) {
                    BlockNode parent = nodes.get(parentHash);
                    BlockNode node = parent.addNode(block);
                    nodes.put(nodeHash, node);
                    updateLongestLeaf(node);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }


    public int getMaxBranchDepth() {
        return longestLeaf == null ? 0 : longestLeaf.getHeight() - root.getHeight();
    }

    public UTXOEntry[] getLongestBranchUTXOSet(String publicKeyHash) {
        return longestLeaf.getUtxoSet().getUTXOsAvailableForPublicKey(publicKeyHash).toArray(new UTXOEntry[0]);
    }

    private void updateLongestLeaf(BlockNode node) {
        this.longestLeaf = node.getHeight() > longestLeaf.getHeight() ? node : longestLeaf;
    }

    /**
     * Helper function intended to help in advancing in transient ledger tree
     * P.S., It takes linear time in worst case scenario, O(h) time
     *
     * @param root root node of the transient ledger tree
     * @param leaf longest leaf from the root
     * @return second node from the root in the path between root and leaf
     */
    private BlockNode getNextCandidateNodeFromRoot(BlockNode root, BlockNode leaf) {
        BlockNode currentNode = leaf;
        while (!currentNode.getParent().equals(root)) {
            currentNode = currentNode.getParent();
        }
        return currentNode;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        TransactionInput i1 =  new TransactionInput();
        TransactionInput i2 =  new TransactionInput();


        TransactionOutput o1 = new TransactionOutput();
        TransactionOutput o2 = new TransactionOutput();
        TransactionOutput o3 = new TransactionOutput();
        TransactionOutput o6 = new TransactionOutput();
        TransactionOutput o7 = new TransactionOutput();
        TransactionOutput o8 = new TransactionOutput();
        TransactionOutput o9 = new TransactionOutput();
        TransactionOutput o10 = new TransactionOutput();

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

        o10.amount = 10000;
        o10.publicKeyHash = "Ah yaaany".getBytes();

        Transaction t0 = new SpecialTransaction(4);

        t0.setTransactionOutputs(new TransactionOutput[]{o6, o7 , o8, o9});

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

        Transaction t1 = new NormalTransaction(2,2);

        t1.setTransactionInputs( new TransactionInput[]{i1, i2});
        t1.setTransactionOutputs( new TransactionOutput[]{o1, o2});

        Transaction t2 = new NormalTransaction(2, 3);



        TransientPartitionTree tree = new TransientPartitionTree();
        // Block 1
        Block b1 = new Block(0);
        b1.hash = new byte[1];
        b1.hash[0] = 0;
        BlockHeader h1 = new BlockHeader();
        h1.hashOfPrevBlock = new byte[1];
        h1.hashOfPrevBlock[0] = 1;
        b1.setHeader(h1);

        b1.setTransactions(new Transaction[]{t0});

        tree.addBlock(b1);

        // Block 2
        Block b2 = new Block(0);
        b2.hash = new byte[1];
        b2.hash[0] = 1;
        BlockHeader h2 = new BlockHeader();
        h2.hashOfPrevBlock = new byte[1];
        h2.hashOfPrevBlock[0] = 0;
        b2.setHeader(h2);


        tree.addBlock(b2);

        // Block 3
        Block b3 = new Block(0);
        b3.hash = new byte[1];
        b3.hash[0] = 2;
        BlockHeader h3 = new BlockHeader();
        h3.hashOfPrevBlock = new byte[1];
        h3.hashOfPrevBlock[0] = 0;
        b3.setHeader(h3);

        b3.setTransactions(new Transaction[]{t1});


        tree.addBlock(b3);

        // Block 4
        Block b4 = new Block(0);
        b4.hash = new byte[1];
        b4.hash[0] = 3;
        BlockHeader h4 = new BlockHeader();
        h4.hashOfPrevBlock = new byte[1];
        h4.hashOfPrevBlock[0] = 1;
        b4.setHeader(h4);
        b4.setTransactions(new Transaction[]{t1});

        tree.addBlock(b4);

        System.out.println("ledger length: " + tree.getMaxBranchDepth());

        tree.removeFirstBlock();

        System.out.println("ledger length: " + tree.getMaxBranchDepth());
    }

}
