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
            List<String> lines = getLines();
            List<Pair<String, Integer>> peers = getPeers(lines);
            initiateConnections(peers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getLines() throws IOException {
        File file = new FileLoader().getFile(Configs.PEERS_FILE_PATH);
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

    private void initiateConnections(List<Pair<String, Integer>> peers) {
        peers.forEach(p -> {
            CommunicationUnit cu = new CommunicationUnit();
            cu.setEvent(Events.ADDRESS);
            cu.setSocketAddress(p.getKey());
            cu.setSocketPort(p.getValue());
            process.invokeClientEvent(cu);
        });
    }
}
