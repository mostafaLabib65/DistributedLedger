package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA {

    public static byte[] getSHA(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input);
    }
}
