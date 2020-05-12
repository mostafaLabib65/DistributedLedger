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
        addClient(cu.getServerAddress(), cu.getServerPort());
    }

    private void addClient(String address, int port) {
        ActiveClients activeClients = ActiveClients.getActiveClients();
        activeClients.addClient(address, port);
    }

    private void initiateNewConnection(CommunicationUnit cu) throws IOException {
        // Initiate a connection
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.writeObject(cu);
        clientSocket.close();
        outputStream.close();

        // Add the connection to active clients
        addClient(cu.getSocketAddress(), cu.getSocketPort());
    }
}
