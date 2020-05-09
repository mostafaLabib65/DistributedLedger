package network.entities;

import network.events.Events;
import java.io.Serializable;

public class CommunicationUnit implements Serializable {

    // TODO add rest of data structures here
    private Events event;
    private String socketAddress;
    private int socketPort;

    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public String getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(String socketAddress) {
        this.socketAddress = socketAddress;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }
}
