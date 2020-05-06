package network;

import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.mq.MQ;
import network.runnables.ClientRunnable;
import network.runnables.ProcessListener;
import network.runnables.ServerRunnable;

import java.net.InetAddress;

public abstract class Process {

    private ClientRunnable clientRunnable;
    private ServerRunnable serverRunnable;
    private ProcessListener processListener;
    MQ processClientMQ;
    int port;
    InetAddress address;

    public Process(int port, InetAddress address){
        MQ serverProcessMQ = new MQ(Configs.MAX_MQ_LENGTH);
        processClientMQ = new MQ(Configs.MAX_MQ_LENGTH);
        serverRunnable = new ServerRunnable(port, address, serverProcessMQ);
        clientRunnable = new ClientRunnable(processClientMQ);
        processListener = new ProcessListener(serverProcessMQ, this);
        this.port = port;
        this.address = address;
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

    public abstract void handleEvent(CommunicationUnit cu);

    public abstract void initiateConnection();
}
