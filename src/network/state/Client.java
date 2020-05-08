package network.state;

import DataStructures.Ledger.Ledger;
import DataStructures.Ledger.UTXOEntry;
import DataStructures.Transaction.NormalTransaction;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionInput;
import DataStructures.Transaction.TransactionOutput;
import Utils.RSA;
import Utils.BytesConverter;
import Utils.SHA;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

import static network.events.Events.BLOCK;

public class Client implements Subscription.Subscriber {

    private int port = 4000;
    private String address = "127.0.0.1";
    private Process process;
    // Initiate initial conditions
    private int peerPort = 5000;
    private String peerAddress = "192.168.1.2";

    private BigInteger publicKey;
    private BigInteger modulus;
    private RSA rsa;
    private Ledger ledger;
    //TODO add publickeys list
    private List<String> hashedPublicKeys;

    public Client() {
        Subscription.getSubscription().subscribe(BLOCK, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.PUBLISH_PUBLICKEY, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_PUBLICKEYS, this);
        initialize();
        sendTransactions();
    }

    private void sendTransactions() {
        int numOfTransactions = 1; //TODO get random number
        for (int i = 0; i < numOfTransactions; i++) {
            Transaction transaction = createTransaction();
            sendTransaction(transaction);
//            sleep(100);
        }

    }

    private Transaction createTransaction() {
        /*
        * get random number of utxo entries => represent number of transaction input
        * get random coin flip for transaction outputs
        * sum the number of utxos and random for 2 splits if 2 outputs
        *
        * get public key using RSA
        * random pick for reciever
        * done
         */
        int[] numberOfUTXOEntries = {0,1}; //TODO set at random max size below utxoset size
        int numberOfOutputs = 2; //TODO set at random

        Transaction transaction = new NormalTransaction(numberOfUTXOEntries.length, numberOfOutputs);

        TransactionInput[] transactionInputs = new TransactionInput[numberOfUTXOEntries.length];
        String publicKeyString = BytesConverter.byteToHexString(publicKey.toByteArray(), 64);
        long UTXOSummation = 0;
        for (int i = 0; i < numberOfUTXOEntries.length; i++) {

            UTXOEntry entry = ledger.getAvailableUTXOsForPublicKey(publicKeyString)[numberOfUTXOEntries[i]];
            UTXOSummation += entry.transactionOutput.amount;

            TransactionInput input = new TransactionInput();

            input.publicKey = publicKey;
            input.publicKeyModulus = modulus;
            input.outputIndex = entry.outputIndex;
            byte[] tHash = new byte[0];

            try {
                tHash = entry.transaction.getTransactionHash();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            BigInteger tmpI = new BigInteger(1, tHash);
            input.signature = rsa.decrypt(tmpI);
            input.transactionHash = tHash;

            transactionInputs[i] = input;
        }

        transaction.setTransactionInputs(transactionInputs);



        TransactionOutput o1 = new TransactionOutput();
        TransactionOutput o2 = new TransactionOutput(); //TODO perform random split
        o1.amount = UTXOSummation;
        o1.publicKeyHash = hashedPublicKeys.get(0).getBytes(); //TODO get random receiver

        transaction.setTransactionOutputs(new TransactionOutput[]{o1}); //TODO add o2 if random split
        return transaction;
    }

    private void initialize() {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            process = new Process(port, inetAddress);
            process.start();

            createKeys();
            sendPublickey();

            request(Events.REQUEST_PUBLICKEYS);
            request(Events.REQUEST_LEDGER);

        } catch (UnknownHostException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void createKeys() throws NoSuchAlgorithmException {
        rsa = new RSA(2048);
        publicKey = rsa.getPublicKey();
        modulus = rsa.getModulus();
    }

    private void request(Events event) {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(event);
        cu.setSocketPort(peerPort);
        cu.setSocketAddress(peerAddress);
        process.invokeClientEvent(cu);
    }

    private void sendTransaction(Transaction transaction) {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.TRANSACTION);
        cu.setTransaction(transaction);
        cu.setSocketPort(peerPort);
        cu.setSocketAddress(peerAddress);
        process.invokeClientEvent(cu);
    }

    @Override
    public void notify(Events events, CommunicationUnit cu) {
        switch (events) {
            case BLOCK:
                try {
                    ledger.addBlock(cu.getBlock());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                break;
            case RECEIVE_LEDGER:
                ledger = cu.getLedger();
                break;
            case REQUEST_LEDGER:
                sendLedger();
                break;
            case PUBLISH_PUBLICKEY:
                hashedPublicKeys.add(cu.getHashedPublicKey());
                break;
            case REQUEST_PUBLICKEYS:
                sendPublickKeys();
                break;
            case RECEIVE_PUBLICKEYS:
                hashedPublicKeys = cu.getHashedPublicKeys();
                break;
        }
    }

    private void sendPublickKeys() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_PUBLICKEYS);
        cu.setHashedPublicKeys(hashedPublicKeys);
        cu.setSocketPort(peerPort);
        cu.setSocketAddress(peerAddress);
        process.invokeClientEvent(cu);
    }

    private void sendLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.TRANSACTION);
        cu.setLedger(ledger);
        cu.setSocketPort(peerPort);
        cu.setSocketAddress(peerAddress);
        process.invokeClientEvent(cu);
    }

    private void sleep(int time) {
        sleep(time);
        request(Events.REQUEST_LEDGER);
        request(Events.REQUEST_PUBLICKEYS);
    }

    private void sendPublickey() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.PUBLISH_PUBLICKEY);
        ArrayList<byte[]> tmp = new ArrayList<>();
        tmp.add(publicKey.toByteArray());
        tmp.add(modulus.toByteArray());
        byte[] pkHash = new byte[0];
        try {
            pkHash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        cu.setHashedPublicKey(BytesConverter.byteToHexString(pkHash, 64));
        cu.setSocketPort(peerPort);
        cu.setSocketAddress(peerAddress);
        process.invokeClientEvent(cu);
    }
}
