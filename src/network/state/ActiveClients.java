package network.state;

import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveClients {

    private ConcurrentHashMap<String, Socket> activeClientsHashMap;
    private static ActiveClients activeClients;

    private ActiveClients(){
        this.activeClientsHashMap = new ConcurrentHashMap<>();
    }

    public static ActiveClients getActiveClients(){
        if(activeClients == null)
            activeClients = new ActiveClients();
        return activeClients;
    }

    public void addClient(String address, Socket socket){
        activeClientsHashMap.put(address, socket);
    }

    public Collection<Socket> getAllActiveSockets() { return activeClientsHashMap.values(); }
}
