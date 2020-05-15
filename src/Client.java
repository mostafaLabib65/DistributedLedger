import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Ledger.UTXOEntry;
import DataStructures.Transaction.NormalTransaction;
import DataStructures.Transaction.Transaction;
import DataStructures.Transaction.TransactionInput;
import DataStructures.Transaction.TransactionOutput;
import Utils.BytesConverter;
import Utils.RSA;
import Utils.SHA;
import network.Process;
import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.lang.Thread.sleep;

public class Client implements Subscription.Subscriber {


    private int port;
    private Process process;

    private ClientBlockAdder blockAdder;
    private Thread blockAdderThread;
    protected ArrayList<Block> addBlocksToLedgerQueue = new ArrayList<>();
    private UTXOEntry[] UTXOSet;
    private Set<UTXOEntry> usedUTXO;

    private BigInteger publicKey;
    private String publicKeyString;
    private BigInteger modulus;
    private RSA rsa;
    private Ledger ledger;
    private List<String> hashedPublicKeys = new LinkedList<>();
    private Random rand;

    public Client(int port) {
        this.port = port;
        Subscription.getSubscription().subscribe(Events.BLOCK, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.RECEIVE_LEDGER, this);
        Subscription.getSubscription().subscribe(Events.PUBLISH_PUBLICKEY, this);
        Subscription.getSubscription().subscribe(Events.REQUEST_PUBLICKEYS, this);
        Subscription.getSubscription().subscribe(Events.RECEIVE_PUBLICKEYS, this);
        initialize();
        initializeBlockAdderToLedgerService();
        busyWaiting();
        sendTransactions();
    }

    private void initializeBlockAdderToLedgerService(){
        this.blockAdder = new ClientBlockAdder(this.addBlocksToLedgerQueue, this.ledger, this.process);
        this.blockAdderThread = new Thread(this.blockAdder);
        this.blockAdderThread.start();
    }

    private void busyWaiting() {
        System.out.println("Waiting for ledger and public keys");
        while (ledger == null || hashedPublicKeys.isEmpty()){
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Busy waiting finished");
    }

    private void sendTransactions() {
        System.out.println("Start making transactions");
        int numOfTransactions = rand.nextInt(5);

        for (int i = 0; i < numOfTransactions; i++) {
            if(ledger != null) {
                Transaction transaction = createTransaction();
                if (transaction != null){
                    sendTransaction(transaction);
                    System.out.println("Transaction was sent");
                }
            }
//            sleep(100);
        }

    }

    private Transaction createTransaction() {

        String publicKeyString = getHashedPublicKey();

        if(UTXOSet == null || UTXOSet.length == 0) {
            return null;
        }

        List<UTXOEntry> chosenEntries = getNumberOfUTXOChosen();

        int numberOfOutputs = rand.nextInt(1) + 1; //number of outputs 1 or 2

        Transaction transaction = new NormalTransaction(chosenEntries.size(), numberOfOutputs);

        TransactionInput[] transactionInputs = new TransactionInput[chosenEntries.size()];
        long UTXOSummation = 0;
        for (int i = 0; i < chosenEntries.size(); i++) {

            UTXOEntry entry = chosenEntries.get(i);
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
            o1.publicKeyHash = BytesConverter.hexStringToByteArray(hashedPublicKeys.get(receiverKey));
            transaction.setTransactionOutputs(new TransactionOutput[]{o1});

        } else if (numberOfOutputs == 2) {
            float randomSplit = rand.nextFloat();

            o1.amount = (long) (UTXOSummation*randomSplit);
            o1.publicKeyHash = BytesConverter.hexStringToByteArray(hashedPublicKeys.get(receiverKey));
            o2.amount = (long) (UTXOSummation*(1.0 - randomSplit));
            o2.publicKeyHash = BytesConverter.hexStringToByteArray(publicKeyString);

            transaction.setTransactionOutputs(new TransactionOutput[]{o1, o2});

        } else {
            throw new RuntimeException("Incorrect number of outputs");
        }
        return transaction;
    }

    private List<UTXOEntry> getNumberOfUTXOChosen() {

        int bound = rand.nextInt(UTXOSet.length - 1) + 1;

        List<UTXOEntry> chosenEntries = new ArrayList<>();

        for(UTXOEntry entry : UTXOSet) {
            if(chosenEntries.size() == bound) {
                break;
            }
            if(!usedUTXO.contains(entry)) {
                chosenEntries.add(entry);
                usedUTXO.add(entry);
            }
        }
        return chosenEntries;
    }

    private void initialize() {

        usedUTXO = new HashSet<>();
        rand = new Random(System.currentTimeMillis());

        try {
            String address = "127.0.0.1";
            InetAddress inetAddress = InetAddress.getByName(address);
            process = new Process(port, inetAddress);
            process.start();

            createKeys();
            sendPublickey();
            request(Events.REQUEST_PUBLICKEYS);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void createKeys() {
        rsa = new RSA(2048);
        publicKey = rsa.getPublicKey();
        modulus = rsa.getModulus();
        publicKeyString = BytesConverter.byteToHexString(publicKey.toByteArray(), 64);

    }

    private void request(Events event) {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(event);
        process.invokeClientEvent(cu);
    }

    private void sendTransaction(Transaction transaction) {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.TRANSACTION);
        cu.setTransaction(transaction);
        process.invokeClientEvent(cu);
    }

    @Override
    public void notify(Events events, CommunicationUnit cu) {
        switch (events) {
            case BLOCK:
                System.out.println("Block Received");

                this.addBlocksToLedgerQueue.add(cu.getBlock());
                try {
                    UTXOSet = ledger.getAvailableUTXOsForPublicKey(getHashedPublicKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(this.addBlocksToLedgerQueue.size() == 1)
                    this.blockAdderThread.interrupt();
                break;
            case RECEIVE_LEDGER:
                System.out.println("Received Ledger");

                if(ledger == null || cu.getLedger().getLedgerDepth() >= ledger.getLedgerDepth()){
                    System.out.println("Ledger accepted");
                    ledger = cu.getLedger();
                    try {
                        UTXOSet = ledger.getAvailableUTXOsForPublicKey(getHashedPublicKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    blockAdder.setLedger(ledger);
                    if(blockAdder.waitingForLedger){
                        this.blockAdderThread.interrupt();
                    }
                }
                break;
            case REQUEST_LEDGER:
                    sendLedger();
                break;
            case REQUEST_PUBLICKEYS:
                System.out.println("Received public keys request: sending it...");
                this.sendPublickey();
                break;
            case RECEIVE_PUBLICKEYS:
                hashedPublicKeys.add(cu.getHashedPublicKey());
                break;
        }
    }

//    private void sendPublickKeys() {
//        CommunicationUnit cu = new CommunicationUnit();
//        cu.setEvent(Events.RECEIVE_PUBLICKEYS);
//        cu.setHashedPublicKeys(hashedPublicKeys);
//
//        process.invokeClientEvent(cu);
//    }

    private void sendLedger() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_LEDGER);
        cu.setLedger(ledger);

        process.invokeClientEvent(cu);
    }

//    private void sleep(int time) {
//        sleep(1000000);
//        request(Events.REQUEST_LEDGER);
//        request(Events.REQUEST_PUBLICKEYS);
//    }



    protected String getHashedPublicKey(){
        ArrayList<byte[]> tmp = new ArrayList<>();
        tmp.add(publicKey.toByteArray());
        tmp.add(modulus.toByteArray());

        byte[] pkHash = new byte[0];
        try {
            pkHash = SHA.getSHA(BytesConverter.concatenateByteArrays(tmp));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return BytesConverter.byteToHexString(pkHash, 64);
    }

    protected void sendPublickey() {
        CommunicationUnit cu = new CommunicationUnit();
        cu.setEvent(Events.RECEIVE_PUBLICKEYS);
        cu.setHashedPublicKey(getHashedPublicKey());
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

        String[] str = {"abc", "bks", "asasa", "sasasa","bks",  "sasas"};
        List<String> list = new ArrayList<>(Arrays.asList(str));
        int[] numberOfUTXOEntriesChosen = {1,2,4};
        for (int i = 0; i < numberOfUTXOEntriesChosen.length; i++) {
            list.set(numberOfUTXOEntriesChosen[i], null);
        }
        while(list.remove(null));
        str =  Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
        System.out.println(Arrays.toString(str));
    }

}
