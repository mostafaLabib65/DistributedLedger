package Nodes.Miner;

import Nodes.Consensus.Consensus;
import network.entities.CommunicationUnit;
import network.events.Events;

public class POWMiner extends Miner{
    public POWMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader) {
        super(blockConsumer, blockSize, address, port, leader);
    }

    @Override
    public void notify(Events event, CommunicationUnit cu) {
        switch (event) {
            case TRANSACTION:
                serveTransactionEvent(cu);
                break;
            case RECEIVE_LEDGER:
                ledger = cu.getLedger();
                break;
            case BLOCK:
                this.addBlock(cu.getBlock());
                break;

            case REQUEST_LEDGER:
                sendLedger();
                break;
        }
    }
}
