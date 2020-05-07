package network;

import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.factory.HandlerFactory;
import network.mq.MQ;
import network.runnables.ClientRunnable;
import network.runnables.ProcessListener;
import network.runnables.ServerRunnable;

import java.net.InetAddress;

public class Process {

    private ClientRunnable clientRunnable;
    private ServerRunnable serverRunnable;
    private ProcessListener processListener;
    private MQ processClientMQ;

    public Process(int port, InetAddress address){
        MQ serverProcessMQ = new MQ(Configs.MAX_MQ_LENGTH);
        processClientMQ = new MQ(Configs.MAX_MQ_LENGTH);
        serverRunnable = new ServerRunnable(port, address, serverProcessMQ);
        clientRunnable = new ClientRunnable(processClientMQ);
        processListener = new ProcessListener(serverProcessMQ, this);
    }


    public void start(){
        // Starting Communication Threads
        startThreads();
    }

    private void startThreads(){
        new Thread(serverRunnable).start();
        new Thread(clientRunnable).start();
        new Thread(processListener).start();
    }

    public void invokeClientEvent(CommunicationUnit cu){
        try {
            processClientMQ.putMessage(cu);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleServerEvent(CommunicationUnit cu){
        HandlerFactory.getHandler(cu.getEvent()).handleIncoming(cu);
    }
}
