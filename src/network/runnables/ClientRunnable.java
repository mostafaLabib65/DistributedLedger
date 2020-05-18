package network.runnables;

import Nodes.MinerUtils.Logger;
import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.events.Events;
import network.factory.HandlerFactory;
import network.mq.MQ;

import java.io.IOException;

// Always Broadcasts
public class ClientRunnable implements Runnable{

    private MQ processClientMQ;
    private Logger logger;
    public ClientRunnable(MQ processClientMQ){

        this.processClientMQ = processClientMQ;
        try {
            this.logger = new Logger("Blocks Client Runnable");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            while(true){
                CommunicationUnit cu = processClientMQ.getMessage();
                HandlerFactory.getHandler(cu.getEvent()).handleOutgoing(cu);
                if(cu.getEvent() == Events.BLOCK || cu.getEvent() == Events.RECEIVE_LEDGER || cu.getEvent() == Events.REQUEST_LEDGER){
                    try {
                        logger.log_block_num(Configs.indicator + "1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
