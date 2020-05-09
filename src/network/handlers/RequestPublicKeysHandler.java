package network.handlers;

import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

public class RequestPublicKeysHandler implements Handler {
    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        Subscription.getSubscription().notify(Events.REQUEST_PUBLICKEYS, cu);
    }
}
