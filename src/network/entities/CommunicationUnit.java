package network.entities;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import DataStructures.Transaction.Transaction;
import network.events.Events;

import java.io.Serializable;

public class CommunicationUnit implements Serializable {

    // TODO add rest of data structures here
    private Events event;
    private String socketAddress;
    private int socketPort;
    private Ledger ledger;
    private Block block;
    private Transaction transaction;
    private boolean BFTVote;
    private String hashedPublicKey;
    private String hashedPublicKeys;
    private String serverAddress;
    private int serverPort;

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

    public boolean getBFTVote(){ return BFTVote; }

    public void setBFTVote(boolean msg) { this.BFTVote = msg; }
  
    public String getHashedPublicKey() {
        return hashedPublicKey;
    }

    public void setHashedPublicKey(String hashedPublicKey) {
        this.hashedPublicKey = hashedPublicKey;
    }

    public String getHashedPublicKeys() {
        return hashedPublicKeys;
    }

    public void setHashedPublicKeys(String hashedPublicKeys) {
        this.hashedPublicKeys = hashedPublicKeys;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
