package network.runnables;

import network.Process;
import network.entities.CommunicationUnit;
import network.mq.MQ;

public class ProcessListener implements Runnable {

    private MQ serverProcessMQ;
    private Process process;

    public ProcessListener(MQ serverProcessMQ, Process process){
        this.serverProcessMQ = serverProcessMQ;
        this.process = process;
    }

    @Override
    public void run() {
        try {
            while (true) {
                CommunicationUnit cu = serverProcessMQ.getMessage();
                process.handleServerEvent(cu);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
