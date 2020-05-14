package DataStructures.Transaction;

import Utils.BytesConverter;
import Utils.SHA;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

public abstract class Transaction implements Serializable {

    private int version = 1;
    private int inputCounter = 0;
    private int outputCounter = 0;
    private int lockTime = 0;

    private TransactionInput[] transactionInputs;
    private TransactionOutput[] transactionOutputs;



    public Transaction(int inputCount, int outputCount){

        inputCounter = inputCount;
        outputCounter = outputCount;

        transactionInputs = new TransactionInput[inputCount];
        transactionOutputs = new TransactionOutput[outputCount];

    }



    public int getInputCounter() {
        return inputCounter;
    }

    public int getOutputCounter() {
        return outputCounter;
    }

    public void setTransactionOutputs(TransactionOutput[] transactionOutputs) {
        outputCounter = transactionOutputs.length;
        this.transactionOutputs = transactionOutputs;
    }

    public void setTransactionInputs(TransactionInput[] transactionInputs) {
        inputCounter = transactionInputs.length;
        this.transactionInputs = transactionInputs;
    }

    public TransactionOutput[] getTransactionOutputs() {
        return transactionOutputs;
    }

    public TransactionInput[] getTransactionInputs() {
        return transactionInputs;
    }

    public byte[] getTransactionHash() throws NoSuchAlgorithmException {

        ArrayList<byte[]> fieldsBytes = new ArrayList<>();
        fieldsBytes.add(BytesConverter.intToBytes(version));
        fieldsBytes.add(BytesConverter.intToBytes(inputCounter));
        fieldsBytes.add(BytesConverter.intToBytes(outputCounter));
        fieldsBytes.add(BytesConverter.intToBytes(lockTime));

        for (int i = 0; i < transactionInputs.length ; i++) {
            fieldsBytes.add(transactionInputs[i].getByteRepresentation());
        }

        for (int i = 0; i < transactionOutputs.length ; i++) {
            fieldsBytes.add(transactionOutputs[i].getByteRepresentation());
        }

        return SHA.getSHA(BytesConverter.concatenateByteArrays(fieldsBytes));

    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        TransactionInput i1 =  new TransactionInput();
        TransactionInput i2 =  new TransactionInput();
        TransactionInput i3 =  new TransactionInput();
        TransactionInput i4 =  new TransactionInput();
        TransactionInput i5 =  new TransactionInput();

        i1.publicKey = BigInteger.probablePrime(256, new SecureRandom());
        i1.publicKeyModulus = BigInteger.probablePrime(256, new SecureRandom());
        i1.signature = BigInteger.probablePrime(256, new SecureRandom());
        i1.transactionHash = "Ah ya 7osty l soda yany yama".getBytes();

        i2.publicKey = BigInteger.probablePrime(256, new SecureRandom());
        i2.publicKeyModulus = BigInteger.probablePrime(256, new SecureRandom());
        i2.signature = BigInteger.probablePrime(256, new SecureRandom());
        i2.transactionHash = "Ah ya 7osty l soda yany yama".getBytes();

        i3.publicKey = BigInteger.probablePrime(256, new SecureRandom());
        i3.publicKeyModulus = BigInteger.probablePrime(256, new SecureRandom());
        i3.signature = BigInteger.probablePrime(256, new SecureRandom());
        i3.transactionHash = "Ah ya 7osty l soda yany yama".getBytes();

        i4.publicKey = BigInteger.probablePrime(256, new SecureRandom());
        i4.publicKeyModulus = BigInteger.probablePrime(256, new SecureRandom());
        i4.signature = BigInteger.probablePrime(256, new SecureRandom());
        i4.transactionHash = "Ah ya 7osty l soda yany yama".getBytes();



        TransactionOutput o1 = new TransactionOutput();
        TransactionOutput o2 = new TransactionOutput();
        TransactionOutput o3 = new TransactionOutput();
        TransactionOutput o4 = new TransactionOutput();
        TransactionOutput o5 = new TransactionOutput();


        o1.amount = 5000;
        o1.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o2.amount = 5000;
        o2.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o3.amount = 5000;
        o3.publicKeyHash = "Ah ya 7osty l soda yany yama".getBytes();

        o4.amount = 5000;
        o4.publicKeyHash = "Ah ya 7osty l soda yany yAma".getBytes();

        Transaction t1 = new NormalTransaction(3,3);
        Transaction t2 = new NormalTransaction(3,3);

        t1.transactionInputs = new TransactionInput[]{i1, i2, i3};
        t2.transactionInputs = new TransactionInput[]{i1, i2, i3};

        t1.transactionOutputs = new TransactionOutput[]{o1, o2 , o3};
        t2.transactionOutputs = new TransactionOutput[]{o1, o2 , o3};



        byte[] h1 = t1.getTransactionHash();
        byte[] h2 = t2.getTransactionHash();


        System.out.println(BytesConverter.byteToHexString(h1, 32));
        System.out.println(BytesConverter.byteToHexString(h2, 32));

    }


    public abstract boolean isValidOutputCount();

    public abstract boolean validateInputOutputDifference(long sum);

}
