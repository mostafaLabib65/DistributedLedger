package network.runnables;

import network.entities.CommunicationUnit;
import network.factory.HandlerFactory;
import network.mq.MQ;

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
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
