package network;

import network.entities.CommunicationUnit;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;

public class Broadcaster {

    public static void broadcast(CommunicationUnit cu){
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
}
