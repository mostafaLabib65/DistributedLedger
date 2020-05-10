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
    private boolean BFTMsg;

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

    public boolean getBFTMsg(){ return BFTMsg; }

    public void setBFTMsg(boolean msg) { this.BFTMsg = msg; }
}
