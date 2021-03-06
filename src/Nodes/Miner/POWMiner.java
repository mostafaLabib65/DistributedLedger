package Nodes.Miner;

import DataStructures.Ledger.Ledger;
import Nodes.Consensus.Consensus;
import network.entities.CommunicationUnit;
import network.events.Events;

import static network.events.Events.REQUEST_PUBLICKEYS;

public class POWMiner extends Miner{
    public POWMiner(Consensus blockConsumer, int blockSize, String address, String globalAddress, int port, boolean leader, int numOfParticipants) {
        super(blockConsumer, blockSize, address, globalAddress, port, leader, numOfParticipants);
        initializeSubscriptions();
        if(leader)
            ledger = new Ledger();
        initializeBlockConsumerService();
        initializeBlockProducerService();
        initializeBlockAdderToLedgerService();
        if(leader){
            request(REQUEST_PUBLICKEYS);
        }
        sendPublickey();
    }



    @Override
    public void notify(Events event, CommunicationUnit cu) {
        switch (event) {
            case TRANSACTION:
                serveTransactionEvent(cu);
                break;
            case RECEIVE_LEDGER:
                System.out.println("POW Miner: received Ledger");
                if(ledger == null || this.blockAdder.waitingForLedger){
                    System.out.println("POW Miner: Accepting ledger");
                    ledger = cu.getLedger();
                    this.blockConsumer.setLedger(ledger);
                    this.blockAdder.setLedger(ledger);
                    this.blockAdderThread.interrupt();
                    this.blockConsumerThread.interrupt();
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
                if(hashedPublicKeys.size() == numOfParticipants-1){
                    hashedPublicKeys.add(getHashedPublicKey());
                    this.blockProducerThread.interrupt();
                }
                break;
        }
    }
}
