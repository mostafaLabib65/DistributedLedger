package network;

import network.entities.CommunicationUnit;
import network.events.Events;
import network.state.Subscription;

public class MinerMock implements Subscription.Subscriber {

    public MinerMock(){
        Subscription.getSubscription().subscribe(Events.TRANSACTION, this);
    }

    @Override
    public void notify(Events events, CommunicationUnit cu) {

    }
}
