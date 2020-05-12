package network.handlers;

import network.utils.Broadcaster;
import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

public class TransactionHandler implements Handler {
    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        Broadcaster.broadcast(cu);
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        Subscription.getSubscription().notify(Events.TRANSACTION, cu);
    }
}
