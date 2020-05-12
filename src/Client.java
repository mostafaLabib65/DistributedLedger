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
import network.state.Subscription;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static network.events.Events.*;

public class Client implements Subscription.Subscriber {


    private int port = 4000;
    private String address = "127.0.0.1";
    private Process process;
    private int peerPort = 5000;
    private String peerAddress = "192.168.1.2";

    private BigInteger publicKey;
    private BigInteger modulus;
    private RSA rsa;
    private Ledger ledger;
    private List<String> hashedPublicKeys;
    private Random rand;

    public Client() {
        Subscription.getSubscription().subscribe(BLOCK, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.PUBLISH_PUBLICKEY, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_PUBLICKEYS, this);
        initialize();
        busyWaiting();
        sendTransactions();
    }

    private void busyWaiting() {
        while(ledger == null || hashedPublicKeys.isEmpty()) {
            sleep(100);
        }
    }

    private void sendTransactions() {
        int numOfTransactions = rand.nextInt(5);

        for (int i = 0; i < numOfTransactions; i++) {
            if(ledger != null) {
                Transaction transaction = createTransaction();
                if (transaction != null)
                    sendTransaction(transaction);
            }
            sleep(100);
        }

    }

    private Transaction createTransaction() {

        String publicKeyString = BytesConverter.byteToHexString(publicKey.toByteArray(), 64);

        UTXOEntry[] UTXOSet = ledger.getAvailableUTXOsForPublicKey(publicKeyString);

        if(UTXOSet.length == 0) {
            return null;
        }

        int numOfUTXOChosen = rand.nextInt(UTXOSet.length);

        int[] numberOfUTXOEntriesChosen = getNumberOfUTXOChosen(UTXOSet.length, numOfUTXOChosen);
        int numberOfOutputs = rand.nextInt(1) + 1; //number of outputs 1 or 2

        Transaction transaction = new NormalTransaction(numberOfUTXOEntriesChosen.length, numberOfOutputs);

        TransactionInput[] transactionInputs = new TransactionInput[numberOfUTXOEntriesChosen.length];
        long UTXOSummation = 0;
        for (int i = 0; i < numberOfUTXOEntriesChosen.length; i++) {

            UTXOEntry entry = UTXOSet[numberOfUTXOEntriesChosen[i]];
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
        TransactionOutput o2 = new TransactionOutput();
        int receiverKey = rand.nextInt(hashedPublicKeys.size());
        if(numberOfOutputs == 1) {
            o1.amount = UTXOSummation;
            o1.publicKeyHash = hashedPublicKeys.get(receiverKey).getBytes();

            transaction.setTransactionOutputs(new TransactionOutput[]{o1});

        } else if (numberOfOutputs == 2) {
            float randomSplit = rand.nextFloat();

            o1.amount = (long) (UTXOSummation*randomSplit);
            o1.publicKeyHash = hashedPublicKeys.get(receiverKey).getBytes();
            o2.amount = (long) (UTXOSummation*(1.0 - randomSplit));
            o2.publicKeyHash = publicKeyString.getBytes();

            transaction.setTransactionOutputs(new TransactionOutput[]{o1, o2});

        } else {
            throw new RuntimeException("Incorrect number of outputs");
        }

        return transaction;
    }

    private int[] getNumberOfUTXOChosen(int bound, int sizeOfArray) {
        return rand.ints(0, bound)
                .boxed()
                .distinct()
                .limit(sizeOfArray)
                .mapToInt((Integer i) -> i.intValue())
                .toArray();
    }

    private void initialize() {

        rand = new Random(System.currentTimeMillis());

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            process = new Process(port, inetAddress);
            process.start();

            createKeys();
            sendPublickey();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void createKeys() {
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
                    if(!ledger.addBlock(cu.getBlock())) {
                        request(Events.REQUEST_PUBLICKEYS);
                        request(Events.REQUEST_LEDGER);
                    }
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
        cu.setEvent(Events.RECEIVE_LEDGER);
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

    public static void main(String... args) {
//        Random random = new Random(0);

//        int[] myArray = random.ints(0, 1)
//                .boxed()
//                .distinct()
//                .limit(1)
//                .mapToInt( (Integer i) -> i.intValue())
//                .toArray();

//        System.out.println(Arrays.toString(myArray));



    }

}


