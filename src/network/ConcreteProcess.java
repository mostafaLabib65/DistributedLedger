package network;

import network.entities.CommunicationUnit;
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
}
