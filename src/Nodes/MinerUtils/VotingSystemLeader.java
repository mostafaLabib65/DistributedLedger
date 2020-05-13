package Nodes.MinerUtils;

import DataStructures.Block.Block;
import network.Process;
import network.entities.CommunicationUnit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class VotingSystemLeader implements Runnable  {


    private ArrayList<Block> readyForVotingBlocks;
    private CommunicationUnit cu;
    private Block receivedBlock;
    private Process process;
    private boolean waitingForResult = false;
    private Block b;
    public VotingSystemLeader(ArrayList<Block> readyForVotingBlocks, Process process, CommunicationUnit cu){
        this.readyForVotingBlocks = readyForVotingBlocks;
        this.cu = cu;
        this.process = process;
    }


    public void setReceivedBlock(Block receivedBlock){
        this.receivedBlock = receivedBlock;
    }
    @Override
    public void run() {
        while (true){
            synchronized (this){
                while (readyForVotingBlocks.size() == 0 && !waitingForResult){
                    try {
                        System.out.println("Voting System: Waiting for a block");
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("Voting System: Block received");
                        e.printStackTrace();
                    }
                }
                if(!waitingForResult){
                    b = readyForVotingBlocks.get(0);
                    readyForVotingBlocks.remove(b);
                    cu.setBlock(b);
                    process.invokeClientEvent(cu);
                    waitingForResult = true;
                }
                try {
                    System.out.println("Voting System: Waiting for a Voting result");
                    wait();
                } catch (InterruptedException e) {
                    try {
                        if(b.getHash() == receivedBlock.getHash()){
                            System.out.println("Voting System: Voting finished");
                            waitingForResult = false;
                        }else {
                            System.out.println("Voting System: Vote received but not settled yet");
                        }
                    } catch (NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
