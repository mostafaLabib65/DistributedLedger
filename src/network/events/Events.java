package network.events;

// Process(Client-Server) Events
public enum Events {

    // P2P
    ADDRESS,

    // Block Chain
    BLOCK,
    BFT_RECEIVE_VOTE,
    BFT_REQUEST_VOTE,
    BFT_REQUEST_ELECTION,
    REQUEST_LEDGER,
    RECEIVE_LEDGER,
    TRANSACTION,

    // Validation
    REQUEST_PUBLICKEYS,
    RECEIVE_PUBLICKEYS,
    PUBLISH_PUBLICKEY

    // Elections and Consensus
}
