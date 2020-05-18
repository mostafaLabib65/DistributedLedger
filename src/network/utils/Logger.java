package network.utils;

import files.FileLoader;
import network.entities.Configs;

import java.io.*;

public class Logger {

    public static void putLine(String s) {
        try {
            File file = new FileLoader().getFile(Configs.LOGS_FILE_PATH);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.append(s);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
