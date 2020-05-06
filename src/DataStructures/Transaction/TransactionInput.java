package DataStructures.Transaction;

import Utils.BytesConverter;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TransactionInput {

    public byte[] transactionHash;
    public int outputIndex = 0;
    public BigInteger signature;
    public BigInteger publicKey;
    public BigInteger publicKeyModulus;
    public int sequenceNumber = 0xFFFFFFFF;

    public byte[] getByteRepresentation() {
        ArrayList<byte[]> fieldsBytes = new ArrayList<>();
        fieldsBytes.add(transactionHash);
        fieldsBytes.add(BytesConverter.intToBytes(outputIndex));
        fieldsBytes.add(signature.toByteArray());
        fieldsBytes.add(publicKey.toByteArray());
        fieldsBytes.add(publicKeyModulus.toByteArray());
        fieldsBytes.add(BytesConverter.intToBytes(sequenceNumber));
        return BytesConverter.concatenateByteArrays(fieldsBytes);
    }

    public byte[] getPublicKeyByteRepresentation() {
        ArrayList<byte[]> publicKeyRepresentation = new ArrayList<>();
        publicKeyRepresentation.add(this.publicKey.toByteArray());
        publicKeyRepresentation.add(this.publicKeyModulus.toByteArray());
        return BytesConverter.concatenateByteArrays(publicKeyRepresentation);
    }

    public byte[] getTransactionHash(){
            return transactionHash;
    }






}
