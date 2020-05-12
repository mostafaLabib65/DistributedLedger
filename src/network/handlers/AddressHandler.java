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
        addClient(cu);
    }

    private void addClient(CommunicationUnit cu) {
        ActiveClients activeClients = ActiveClients.getActiveClients();
        activeClients.addClient(cu.getServerAddress(), cu.getServerPort());
    }

    private void initiateNewConnection(CommunicationUnit cu) throws IOException {
        // Initiate a connection
        Socket clientSocket = new Socket(cu.getSocketAddress(), cu.getSocketPort());
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.writeObject(cu);
        clientSocket.close();

        // Add the connection to active clients
        addClient(cu);
    }
}
