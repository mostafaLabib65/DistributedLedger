package Nodes.Miner;

import Nodes.Consensus.Consensus;
import network.entities.CommunicationUnit;
import network.events.Events;

import static java.lang.Thread.sleep;

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
                if(cu.getLedger().getLedgerDepth() >= ledger.getLedgerDepth()){
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


            case REQUEST_PUBLICKEYS:
                this.sendPublickey();
                break;


            case RECEIVE_PUBLICKEYS:
                System.out.println("POW Miner: received public key");
                hashedPublicKeys.add(cu.getHashedPublicKey());
                if(hashedPublicKeys.size() == numOfParticipants-1)
                    hashedPublicKeys.add(getHashedPublicKey());
                this.blockProducerThread.interrupt();
                break;
        }
    }
}
