package network.utils;

import network.entities.CommunicationUnit;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Broadcaster {
    private static ReentrantLock broadcastLock = new ReentrantLock();

    public static void broadcast(CommunicationUnit cu){
//        broadcastLock.lock();
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
//        broadcastLock.unlock();
    }
}
