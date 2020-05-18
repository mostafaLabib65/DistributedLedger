package Nodes.MinerUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class Logger {
    private FileWriter writer;
    private Semaphore writerLock = new Semaphore(1);

    Date date= new Date();
    public Logger() throws IOException {
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        String name = "log-" + ts + ".txt";
        writer = new FileWriter(name);
    }

    public Logger(String name) throws IOException {
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        writer = new FileWriter(name + ts + ".txt");
    }

    public void log_block_time(float executionTime) throws IOException, InterruptedException {
        StringBuilder log = new StringBuilder();
        writerLock.acquire();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        log.append(ts);
        log.append(" ");
        log.append(executionTime);
        log.append("\n");
        writer.write(log.toString());
        writer.flush();
        System.out.println(log.toString());
        writerLock.release();
    }

    public void log_block_num(String s) throws IOException, InterruptedException {
        StringBuilder log = new StringBuilder();
        writerLock.acquire();
        log.append(s);
        log.append("\n");
        writer.write(log.toString());
        writer.flush();
        System.out.println(log.toString());
        writerLock.release();
    }
}
