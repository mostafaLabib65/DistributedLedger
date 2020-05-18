package network.runnables;

import network.entities.CommunicationUnit;
import network.events.Events;
import network.factory.HandlerFactory;
import network.mq.MQ;
import network.utils.Logger;

// Always Broadcasts
public class ClientRunnable implements Runnable{

    private MQ processClientMQ;
    public ClientRunnable(MQ processClientMQ){
        this.processClientMQ = processClientMQ;
    }

    @Override
    public void run() {
        try{
            while(true){
                CommunicationUnit cu = processClientMQ.getMessage();
                HandlerFactory.getHandler(cu.getEvent()).handleOutgoing(cu);
                if(cu.getEvent() == Events.BLOCK || cu.getEvent() == Events.RECEIVE_LEDGER || cu.getEvent() == Events.REQUEST_LEDGER){
                    Logger.putLine("1");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
