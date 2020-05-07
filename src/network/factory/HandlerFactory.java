package network.factory;

import network.events.Events;
import network.handlers.AddressHandler;
import network.handlers.Handler;
import network.handlers.TransactionHandler;

public class HandlerFactory {
    public static Handler getHandler(Events events){
        switch (events){
            case ADDRESS:
                return new AddressHandler();
            case TRANSACTION:
                return new TransactionHandler();
            default:
                throw new RuntimeException("Missing Handler for event: " + events);
        }
    }
}
