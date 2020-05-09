package network.handlers;

import network.entities.CommunicationUnit;

public class LedgerHandler implements Handler {
    @Override
    public void handleOutgoing(CommunicationUnit cu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleIncoming(CommunicationUnit cu) {
        throw new UnsupportedOperationException();
    }
}
