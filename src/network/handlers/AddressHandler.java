package network.handlers;

import network.entities.CommunicationUnit;
import network.state.ActiveClients;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AddressHandler implements Handler {

    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        try {
            initiateNewConnection(cu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        try {
            addClient(cu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClient(CommunicationUnit cu) throws IOException {
        ActiveClients activeClients = ActiveClients.getActiveClients();
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        activeClients.addClient(clientSocket.getInetAddress().getHostAddress(), clientSocket);
    }

    private void initiateNewConnection(CommunicationUnit cu) throws IOException {
        // Initiate a connection
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.writeObject(cu);

        // Add the connection to active clients
        addClient(cu);
    }
}
