package network.runnables;

import Nodes.MinerUtils.Logger;
import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.events.Events;
import network.mq.MQ;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Always Listens
public class ServerRunnable implements Runnable{

    private int port;
    private InetAddress address;
    private MQ serverProcessMQ;
    private Logger logger;

    public ServerRunnable(int port, InetAddress address, MQ serverProcessMQ){
        this.port = port;
        this.address = address;
        this.serverProcessMQ = serverProcessMQ;
        try {
            this.logger = new Logger("Blocks Server Runnable");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port, Configs.BACKLOG, address);
            while (true) {
                try{
                    // Blocking read from the socket
                    Socket server = serverSocket.accept();

                    ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                    CommunicationUnit cu = (CommunicationUnit) in.readObject();

                    String remoteAddress = server.getRemoteSocketAddress().toString();
                    cu.setSocketAddress(getCleanAddress(remoteAddress));
                    cu.setSocketPort(getCleanPort(remoteAddress));

                    serverProcessMQ.putMessage(cu);

                    if(cu.getEvent() == Events.BLOCK || cu.getEvent() == Events.RECEIVE_LEDGER || cu.getEvent() == Events.REQUEST_LEDGER){
                        logger.log_block_num(Configs.indicator + "1");
                    }

                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCleanAddress(String remoteAddress){
        return remoteAddress.split(":")[0].substring(1);
    }

    private int getCleanPort(String remoteAddress){
        return Integer.parseInt(remoteAddress.split(":")[1]);
    }
}
