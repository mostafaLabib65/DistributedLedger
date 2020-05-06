package network.runnables;

import network.entities.CommunicationUnit;
import network.events.Events;
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
                if(cu.getEvent() == Events.RECEIVE_ADDRESS){
                    initiateNewConnection(cu);
                } else {
                    broadcast(cu);
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void initiateNewConnection(CommunicationUnit cu) throws IOException {
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        writeToSocket(clientSocket, cu);
    }

    private void broadcast(CommunicationUnit cu) {
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


    private void writeToSocket(Socket socket, CommunicationUnit cu) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(cu);
    }
}
