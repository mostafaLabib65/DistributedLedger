package DataStructures.Transaction;

import DataStructures.Ledger.UTXOSet;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TransactionFactory {

    public Transaction createRewardTransactionForPublucKey(BigInteger publicExponent, BigInteger modulus) throws NoSuchAlgorithmException {
        ArrayList<byte[]> publicKeyRepresentation = new ArrayList<>();
        publicKeyRepresentation.add(publicExponent.toByteArray());
        publicKeyRepresentation.add(modulus.toByteArray());
        Transaction rewardTransaction = new SpecialTransaction(1);
        TransactionOutput output = new TransactionOutput();
        output.publicKeyHash = SHA.getSHA(BytesConverter.concatenateByteArrays(publicKeyRepresentation));
        output.amount = 625000000;
        rewardTransaction.setTransactionOutputs(new TransactionOutput[]{output});
        return rewardTransaction;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        TransactionFactory f =new TransactionFactory();
        RSA rsa = new RSA(2048);
        Transaction t = f.createRewardTransactionForPublucKey(rsa.getPublicKey(), rsa.getModulus());
        System.out.println(TransactionsValidator.validateSetOfTransactions(new Transaction[]{t}, new UTXOSet()));
    }
}

