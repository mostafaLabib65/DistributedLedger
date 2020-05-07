package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;

public class NetworkMain {
    public static void main(String[] args) {
        try {
            int port = 3000;
            InetAddress address = InetAddress.getByName("127.0.0.1");
            Process process = new ConcreteProcess(port, address);
            process.start();

            // Initiate initial conditions
            int peerPort = 4000;
            String peerAddress = "192.168.1.2";

            process.initiateConnection(peerAddress, peerPort);
            sleep(15000);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
