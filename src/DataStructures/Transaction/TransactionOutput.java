package DataStructures.Transaction;

import Utils.BytesConverter;

import java.io.Serializable;
import java.util.ArrayList;

public class TransactionOutput implements Serializable {

    public long amount;
    public byte[] publicKeyHash;

    public byte[] getByteRepresentation() {
        ArrayList<byte[]> fieldsBytes = new ArrayList<>();
        fieldsBytes.add(BytesConverter.longToBytes(amount));
        fieldsBytes.add(publicKeyHash);
        return BytesConverter.concatenateByteArrays(fieldsBytes);
    }

}
