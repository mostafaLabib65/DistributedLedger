//package network;
//
//import network.entities.CommunicationUnit;
//import network.events.Events;
//import network.utils.ConnectionInitializer;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import static java.lang.Thread.sleep;
//
//public class NetworkMain {
//    public static void main(String[] args) {
//        try {
//            int port = 3000;
//            InetAddress address = InetAddress.getByName("127.0.0.1");
//            Process process = new Process(port, address);
//
//            process.start();
//            ConnectionInitializer connectionInitializer = new ConnectionInitializer(process);
//            connectionInitializer.init();
//            // Initiate initial conditions
//            int peerPort = 4000;
//            String peerAddress = "192.168.1.2";
//
//            CommunicationUnit cu = new CommunicationUnit();
//            cu.setEvent(Events.ADDRESS);
//            cu.setSocketPort(peerPort);
//            cu.setSocketAddress(peerAddress);
//
//            process.invokeClientEvent(cu);
//            sleep(15000);
//
//            cu = new CommunicationUnit();
//            cu.setEvent(Events.TRANSACTION);
//            process.invokeClientEvent(cu);
//
//        } catch (UnknownHostException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
