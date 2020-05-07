package DataStructures.Transaction;

import Utils.BytesConverter;

import java.util.ArrayList;

public class TransactionOutput {

    public long amount;
    public byte[] publicKeyHash;

    public byte[] getByteRepresentation() {
        ArrayList<byte[]> fieldsBytes = new ArrayList<>();
        fieldsBytes.add(BytesConverter.longToBytes(amount));
        fieldsBytes.add(publicKeyHash);
        return BytesConverter.concatenateByteArrays(fieldsBytes);
    }

}
