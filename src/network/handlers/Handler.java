package network.handlers;

import network.entities.CommunicationUnit;

public interface Handler {
    void handleOutgoing(CommunicationUnit cu);
    void handleIncoming(CommunicationUnit cu);
}
