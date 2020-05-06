package network.mq;

import network.entities.CommunicationUnit;

import java.util.Vector;

public class MQ {

    private Vector mq;
    private int MAX;
    public MQ(int size){
        mq = new Vector();
        MAX = size;
    }


    public synchronized void putMessage(CommunicationUnit cu) throws InterruptedException {

        // checks whether the queue is full or not
        while (mq.size() == MAX)
            // waits for the queue to get empty
            wait();

        // then again adds element or messages
        mq.addElement(cu);
        notify();
    }

    public synchronized CommunicationUnit getMessage() throws InterruptedException {
        notify();
        while (mq.size() == 0)
            wait();
        CommunicationUnit cu = (CommunicationUnit) mq.firstElement();

        // extracts the message from the queue
        mq.removeElement(cu);
        return cu;
    }


}
