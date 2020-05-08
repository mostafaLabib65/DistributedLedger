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
                return new LedgerHandler();
            case REQUEST_LEDGER:
                return new LedgerHandler();
            case REQUEST_PUBLICKEYS:
                return new PublickeysHandler();
            case PUBLISH_PUBLICKEY:
                return new PublickeysHandler();
            default:
                throw new RuntimeException("Missing Handler for event: " + events);
        }
    }
}
