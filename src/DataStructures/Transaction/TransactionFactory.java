package DataStructures.Transaction;

import DataStructures.Ledger.UTXOSet;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TransactionFactory {

    public Transaction createRewardTransactionForPublicKey(BigInteger publicExponent, BigInteger modulus) throws NoSuchAlgorithmException {
        ArrayList<byte[]> publicKeyRepresentation = new ArrayList<>();
        publicKeyRepresentation.add(publicExponent.toByteArray());
        publicKeyRepresentation.add(modulus.toByteArray());
        byte[] keyHash = SHA.getSHA(BytesConverter.concatenateByteArrays(publicKeyRepresentation));
        return createSpecialTransactionForPublicKey(keyHash, 625000000);
    }


    public Transaction createSpecialTransactionForPublicKey(byte[] publicKeyHash, long amount) throws NoSuchAlgorithmException {

        Transaction outputTransaction = new SpecialTransaction(1);
        TransactionOutput output = new TransactionOutput();
        output.publicKeyHash = publicKeyHash;
        output.amount = amount;
        outputTransaction.setTransactionOutputs(new TransactionOutput[]{output});
        return outputTransaction;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        TransactionFactory f =new TransactionFactory();
        RSA rsa = new RSA(2048);
        Transaction t = f.createRewardTransactionForPublicKey(rsa.getPublicKey(), rsa.getModulus());
        System.out.println(TransactionsValidator.validateSetOfTransactions(new Transaction[]{t}, new UTXOSet()));
    }
}

