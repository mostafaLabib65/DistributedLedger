package DataStructures.Transaction;

import DataStructures.Ledger.UTXOSet;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;

public class TransactionsValidator {

    public static boolean validateSetOfTransactions(Transaction[] transactions, UTXOSet utxoSet) throws NoSuchAlgorithmException {

        HashSet<String> usedUTXOs = new HashSet<>();
        long sum;

        //TODO -ve amount

        for (int i = 0; i < transactions.length; i++) {

            if (transactions[i].outputCounter != transactions[i].getTransactionOutputs().length)
                return false;
            if (transactions[i].inputCounter != transactions[i].getTransactionInputs().length)
                return false;
            sum = 0;

            if(!transactions[i].isValidOutputCount())
                return false;

            for (int j = 0; j < transactions[i].getTransactionOutputs().length; j++) {
                sum -= transactions[i].getTransactionOutputs()[j].amount;
            }
            for (int j = 0; j < transactions[i].getTransactionInputs().length; j++) {

                TransactionInput input = transactions[i].getTransactionInputs()[j];
                byte[] hash = input.getTransactionHash();

                ArrayList<byte[]> hashAndIndex = new ArrayList<>();

                byte[] index = BytesConverter.intToBytes(input.outputIndex);
                hashAndIndex.add(hash);
                hashAndIndex.add(index);

                byte[] keyBytes = BytesConverter.concatenateByteArrays(hashAndIndex);


                String hashString = BytesConverter.byteToHexString(SHA.getSHA(keyBytes),64);


                if( !utxoSet.contains(hashString))
                    return false;

                if(usedUTXOs.contains(hashString))
                    return false;

                usedUTXOs.add(hashString);

                Transaction transaction = utxoSet.getTransaction(hashString);
                TransactionOutput outputReferenced = transaction.getTransactionOutputs()[input.outputIndex];

                if(!(validateUTXOReference(input, outputReferenced, transaction.getTransactionHash())))
                    return false;
                sum += outputReferenced.amount;

            }

            if(!transactions[i].validateInputOutputDifference(sum)) return false;
        }

        return true;
    }

    private static boolean validateUTXOReference(TransactionInput input,
                                                 TransactionOutput outputReferenced,
                                                 byte[] transactionHash) throws NoSuchAlgorithmException {

        byte[] publicKeyByteRepresentation = input.getPublicKeyByteRepresentation();
        String referencedPublicKeyHash= BytesConverter.byteToHexString(outputReferenced.publicKeyHash, 64);
        String givenPublicKeyRepresentation = BytesConverter.byteToHexString(SHA.getSHA(publicKeyByteRepresentation),64);
        if(!(referencedPublicKeyHash.equals(givenPublicKeyRepresentation)))
            return false;

        String transactionHashString = BytesConverter.byteToHexString(transactionHash, 64);

        BigInteger signedTransactionHash= RSA.decrypt(
                input.signature,
                input.publicKey,
                input.publicKeyModulus
        );

        String signedTransactionHashString = BytesConverter.byteToHexString(
                signedTransactionHash.toByteArray(), 64);
        return transactionHashString.equals(signedTransactionHashString);
    }


}
