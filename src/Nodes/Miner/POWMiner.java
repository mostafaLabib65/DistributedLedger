package Nodes.Miner;

import Nodes.Consensus.Consensus;
import network.entities.CommunicationUnit;
import network.events.Events;

public class POWMiner extends Miner{
    public POWMiner(Consensus blockConsumer, int blockSize, String address, int port, boolean leader, int numOfParticipants) {
        super(blockConsumer, blockSize, address, port, leader, numOfParticipants);
    }



    @Override
    public void notify(Events event, CommunicationUnit cu) {
        switch (event) {
            case TRANSACTION:
                serveTransactionEvent(cu);
                break;
            case RECEIVE_LEDGER:
                if(cu.getLedger().getLegderDepth() >= ledger.getLegderDepth()){
                    ledger = cu.getLedger();
                    this.blockAdderThread.interrupt();
                }
                break;
            case BLOCK:
                this.addBlocksToLedgerQueue.add(cu.getBlock());
                if(this.addBlocksToLedgerQueue.size() == 1)
                    this.blockAdderThread.interrupt();
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
}
