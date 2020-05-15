package network.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveClients {

    private ConcurrentHashMap<String, Integer> activeClientsHashMap;
    private static ActiveClients activeClients;

    private ActiveClients(){
        this.activeClientsHashMap = new ConcurrentHashMap<>();
    }

    public static ActiveClients getActiveClients(){
        if(activeClients == null)
            activeClients = new ActiveClients();
        return activeClients;
    }

    public void addClient(String address, int port){
        activeClientsHashMap.put(address + ":" + port, port);
    }

    public Set<Map.Entry<String, Integer>> getAllActiveClients() {
        return activeClientsHashMap.entrySet();
    }
}
