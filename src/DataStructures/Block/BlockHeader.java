package DataStructures.Block;

import Utils.BytesConverter;
import Utils.SHA;

import java.security.NoSuchAlgorithmException;

import static java.lang.System.currentTimeMillis;

public class BlockHeader {
    public Long timestamp = currentTimeMillis();
    public byte[] hashOfPrevBlock;
    public byte[] hashOfMerkleRoot;
    public int nonce;


    public byte[] hashBlockHeader() throws NoSuchAlgorithmException {
        byte[] timestampBytes = BytesConverter.longToBytes(timestamp);
        byte[] nonceBytes = BytesConverter.intToBytes(nonce);

        byte[] blockHeaderBytes =
                new byte[
                        timestampBytes.length +
                        hashOfPrevBlock.length +
                        hashOfMerkleRoot.length +
                        nonceBytes.length
                        ];

        System.arraycopy(timestampBytes, 0, blockHeaderBytes, 0, timestampBytes.length);
        System.arraycopy(hashOfPrevBlock, 0, blockHeaderBytes, timestampBytes.length, hashOfPrevBlock.length);
        System.arraycopy(hashOfMerkleRoot, 0, blockHeaderBytes,
                timestampBytes.length + hashOfPrevBlock.length
                , hashOfMerkleRoot.length);
        System.arraycopy(nonceBytes, 0, blockHeaderBytes,
                timestampBytes.length + hashOfPrevBlock.length + hashOfMerkleRoot.length,
                nonceBytes.length
        );
        return SHA.getSHA(blockHeaderBytes);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        BlockHeader h1 = new BlockHeader();
        BlockHeader h2 = new BlockHeader();


        h1.hashOfMerkleRoot = "KILL ME PLEASE !".getBytes();
        h1.hashOfPrevBlock = "KILL ME PLEASE !".getBytes();

        h2.hashOfMerkleRoot = "KILL ME PLEASE !".getBytes();
        h2.hashOfPrevBlock = "KILL ME PLEASE !".getBytes();

//        h2.hashOfMerkleRoot = "AH YANY !".getBytes();
//        h2.hashOfPrevBlock = "AH YANY !".getBytes();


        h1.nonce = 7;
//        h2.nonce = 4941684;
        h2.nonce = 7;
        byte[] hash1 = h1.hashBlockHeader();
        byte[] hash2 = h2.hashBlockHeader();

        System.out.println(BytesConverter.byteToHexString(hash1, 8));
        System.out.println(BytesConverter.byteToHexString(hash2, 8));

    }

}



