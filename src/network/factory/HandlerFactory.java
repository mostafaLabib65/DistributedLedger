package network.factory;

import network.events.Events;
import network.handlers.*;

public class HandlerFactory {
    public static Handler getHandler(Events events){
        switch (events){
            case ADDRESS:
                return new AddressHandler();
            case TRANSACTION:
                return new TransactionHandler();
            case BLOCK:
                return new BlockHandler();
            case RECEIVE_LEDGER:
                return new ReceiveLedgerHandler();
            case REQUEST_LEDGER:
                return new RequestLedgerHandler();
            case REQUEST_PUBLICKEYS:
                return new RequestPublicKeysHandler();
            case PUBLISH_PUBLICKEY:
                return new PublishPublicKeyHandler();
            case RECEIVE_PUBLICKEYS:
                return new ReceivePublicKeysHandler();
            case BFT_RECEIVE_VOTE:
                return new BFTReceiveVoteHandler();
            case BFT_REQUEST_VOTE:
                return new BFTRequestVoteHandler();
            case BFT_REQUEST_ELECTION:
                return new BFTRequestElectionHandler();
            default:
                throw new RuntimeException("Missing Handler for event: " + events);
        }
    }
}
