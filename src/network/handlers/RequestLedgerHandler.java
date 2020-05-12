package network.handlers;

import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;
import network.utils.Broadcaster;

public class RequestLedgerHandler implements Handler {
    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        Broadcaster.broadcast(cu);
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        Subscription.getSubscription().notify(Events.REQUEST_LEDGER, cu);
    }
}
