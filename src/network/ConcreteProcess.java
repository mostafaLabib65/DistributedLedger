package network;

import network.entities.CommunicationUnit;
import network.events.Events;

import java.net.InetAddress;

public class ConcreteProcess extends Process {

    public ConcreteProcess(int port, InetAddress address) {
        super(port, address);
    }

    @Override
    public void handleEvent(CommunicationUnit cu) {
        System.out.println(cu.getEvent());
        System.out.println(cu.getSocketAddress());
        System.out.println(cu.getSocketPort());
    }

    @Override
    public void initiateConnection() {
        try {
            CommunicationUnit cu = new CommunicationUnit();
            cu.setEvent(Events.RECEIVE_ADDRESS);
            cu.setSocketPort(port);
            cu.setSocketAddress(address.getHostAddress());
            processClientMQ.putMessage(cu);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
