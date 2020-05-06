package network.runnables;

import network.entities.CommunicationUnit;
import network.mq.MQ;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

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
                Collection<Socket> clientSockets = ActiveClients.getActiveClients().getAllActiveSockets();
                for (Socket clientSocket : clientSockets) {
                    try {
                        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        outputStream.writeObject(cu);
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
