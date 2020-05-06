package Utils;

import java.math.BigInteger;
import java.security.SecureRandom;


public class RSA {
    private final static BigInteger one      = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();

    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;




    public static BigInteger encrypt(BigInteger message, BigInteger key, BigInteger modulus) {
        return message.modPow(key, modulus);
    }


    public static BigInteger decrypt(BigInteger message, BigInteger key, BigInteger modulus) {
        return message.modPow(key, modulus);
    }

    public RSA(int N) {
        BigInteger p = BigInteger.probablePrime(N/2, random);
        BigInteger q = BigInteger.probablePrime(N/2, random);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

        modulus    = p.multiply(q);
        publicKey  = new BigInteger("65537");     // common value in practice = 2^16 + 1
        privateKey = publicKey.modInverse(phi);
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger encrypt(BigInteger message) {
        return message.modPow(publicKey, modulus);
    }

    public BigInteger decrypt(BigInteger encrypted) {
        return encrypted.modPow(privateKey, modulus);
    }

    public String toString() {
        String s = "";
        s += "public  = " + publicKey  + "\n";
        s += "private = " + privateKey + "\n";
        s += "modulus = " + modulus;
        return s;
    }

    public static void main(String[] args) {
        int N = 4096;
        RSA key = new RSA(N);
        System.out.println(key);

        String s = "hello world";
        //// create message by converting string to integer
        // String s = "test";
        // byte[] bytes = s.getBytes();
         BigInteger message = new BigInteger(s.getBytes());

        BigInteger encrypt = key.encrypt(message);
        BigInteger decrypt = key.decrypt(encrypt);
        System.out.println("encrypted = " + encrypt);
        System.out.println("message   = " + message);
        System.out.println("decrypted = " + decrypt);
    }
}
