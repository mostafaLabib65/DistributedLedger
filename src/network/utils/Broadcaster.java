package network.utils;

import network.entities.CommunicationUnit;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class Broadcaster {

    public static void broadcast(CommunicationUnit cu){
        Set<Map.Entry<String, Integer>> clientSockets = ActiveClients.getActiveClients().getAllActiveClients();
        for (Map.Entry<String, Integer> clientSocket : clientSockets) {
            try {
                Socket socket = new Socket(clientSocket.getKey().split(":")[0], clientSocket.getValue());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(cu);
                socket.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
