package Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BytesConverter {

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }


    public static String byteToHexString(byte[] hash, int size) {

        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < size) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }


    public static byte[] concatenateByteArrays(ArrayList<byte[]> arrays) {

        int length = 0;
        int index = 0;
        for (int i = 0; i < arrays.size(); i++) {
            length += arrays.get(i).length;
        }

        byte[] concat = new byte[length];

        for (int i = 0; i < arrays.size(); i++) {
            System.arraycopy(arrays.get(i), 0, concat, index, arrays.get(i).length);
            index += arrays.get(i).length;
        }

        return concat;
    }


}

