package network.handlers;

import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.ActiveClients;
import network.state.Subscription;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;

public class TransactionHandler implements Handler {
    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        broadcast(cu);
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        Subscription.getSubscription().notify(Events.TRANSACTION, cu);
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

}
