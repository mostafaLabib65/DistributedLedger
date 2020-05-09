package network.state;

import network.entities.CommunicationUnit;
import network.events.Events;

import java.util.ArrayList;
import java.util.HashMap;

public class Subscription {
    public interface Subscriber{
        void notify(Events event, CommunicationUnit cu);
    }


    private static Subscription subscription;
    private HashMap<Events, ArrayList<Subscriber>> subsriptionMap;

    private Subscription(){
        subsriptionMap = new HashMap<>();
    }

    public static Subscription getSubscription(){
        if(subscription == null)
            subscription = new Subscription();
        return subscription;
    }

    public void subscribe(Events event, Subscriber subscriber){
        subsriptionMap.computeIfAbsent(event, k -> new ArrayList<>());
        subsriptionMap.get(event).add(subscriber);
    }

    public void notify(Events event, CommunicationUnit cu){
        if(subsriptionMap.get(event) != null)
            subsriptionMap.get(event).forEach(c -> c.notify(event, cu));
    }
}
