package network.utils;

import javafx.util.Pair;
import network.entities.CommunicationUnit;
import network.entities.Configs;
import network.events.Events;
import network.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import files.FileLoader;

public class ConnectionInitializer {

    private Process process;

    public ConnectionInitializer(Process process){
        this.process = process;
    }

    public void init(){
        try {
            List<String> lines = getLines(Configs.PEERS_FILE_PATH);
            List<Pair<String, Integer>> peers = getPeers(lines);
            initiateConnections(peers, false);

            lines = getLines(Configs.LOCAL_PEERS_FILE_PATH);
            peers = getPeers(lines);
            initiateConnections(peers, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getLines(String name) throws IOException {
        File file = new FileLoader().getFile(name);
        BufferedReader br = new BufferedReader(new FileReader(file));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null){
            if(line.charAt(0) != '#')
                lines.add(line);
        }
        br.close();
        return lines;
    }

    private List<Pair<String, Integer>> getPeers(List<String> lines){
        List<Pair<String, Integer>> peers = new ArrayList<>();
        lines.forEach(l -> {
            String[] splitLine = l.split(":");
            peers.add(new Pair<>(splitLine[0], Integer.parseInt(splitLine[1])));
        });

        return peers;
    }

    private void initiateConnections(List<Pair<String, Integer>> peers, boolean isLocal) {
        peers.forEach(p -> {
            CommunicationUnit cu = new CommunicationUnit();
            cu.setEvent(Events.ADDRESS);
            cu.setSocketAddress(p.getKey());
            cu.setSocketPort(p.getValue());
            String tempA = process.getAddress();
            int tempP = process.getPort();
            if(isLocal){
                tempA = p.getKey();
                tempP = p.getValue();
            }
            cu.setServerAddress(tempA);
            cu.setServerPort(tempP);
            process.invokeClientEvent(cu);
        });
    }
}
