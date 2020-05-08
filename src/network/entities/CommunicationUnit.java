package network.entities;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.events.Events;
import java.io.Serializable;
import java.util.List;

public class CommunicationUnit implements Serializable {

    // TODO add rest of data structures here
    private Events event;
    private String socketAddress;
    private int socketPort;
    private Ledger ledger;
    private Block block;
    private Transaction transaction;


    private String hashedPublicKey;
    private List<String> hashedPublicKeys;

    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public String getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(String socketAddress) {
        this.socketAddress = socketAddress;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getHashedPublicKey() {
        return hashedPublicKey;
    }

    public void setHashedPublicKey(String hashedPublicKey) {
        this.hashedPublicKey = hashedPublicKey;
    }

    public List<String> getHashedPublicKeys() {
        return hashedPublicKeys;
    }

    public void setHashedPublicKeys(List<String> hashedPublicKeys) {
        this.hashedPublicKeys = hashedPublicKeys;
    }
}
