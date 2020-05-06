package network.runnables;

import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.events.Events;
import network.mq.MQ;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Always Listens
public class ServerRunnable implements Runnable{

    private int port;
    private InetAddress address;
    private MQ serverProcessMQ;

    public ServerRunnable(int port, InetAddress address, MQ serverProcessMQ){
        this.port = port;
        this.address = address;
        this.serverProcessMQ = serverProcessMQ;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port, Configs.BACKLOG, address);
            while (true) {
                try{
                    // Blocking read from the socket
                    Socket server = serverSocket.accept();

                    ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                    CommunicationUnit cu = (CommunicationUnit) in.readObject();

                    // Add registering client socket to pool of active clients
                    if (cu.getEvent() == Events.RECEIVE_ADDRESS) {
                        addClient(cu);
                    } else {
                        serverProcessMQ.putMessage(cu);
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClient(CommunicationUnit cu) throws IOException {
        ActiveClients activeClients = ActiveClients.getActiveClients();
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        activeClients.addClient(clientSocket.getInetAddress().getHostAddress(), clientSocket);
    }
}
