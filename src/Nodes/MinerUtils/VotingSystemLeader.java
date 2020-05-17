package Nodes.MinerUtils;

import DataStructures.Block.Block;
import Nodes.Miner.BFTMiner;
import network.Process;
import network.entities.CommunicationUnit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import static network.events.Events.BFT_REQUEST_VOTE;

public class VotingSystemLeader implements Runnable  {


    private ArrayList<Block> readyForVotingBlocks;
    private CommunicationUnit cu;
    private Block receivedBlock;
    private Process process;
    private boolean waitingForResult = false;
    private Block b;
    private BFTMiner miner;
    public VotingSystemLeader(ArrayList<Block> readyForVotingBlocks, Process process, BFTMiner miner){
        this.readyForVotingBlocks = readyForVotingBlocks;
        this.cu = new CommunicationUnit();
        this.cu.setEvent(BFT_REQUEST_VOTE);
        this.process = process;
        this.miner = miner;
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
                    }
                }
                if(!waitingForResult){
                    b = readyForVotingBlocks.get(0);
                    readyForVotingBlocks.remove(b);
                    cu.setBlock(b);
                    process.invokeClientEvent(cu);
                    waitingForResult = true;
                    miner.request_vote(b);
                }
                try {
                    System.out.println("Voting System: Waiting for a Voting result");
                    wait();
                } catch (InterruptedException e) {
                    if(receivedBlock != null){
                        try {
                            if(Arrays.equals(b.getHash(), receivedBlock.getHash())){
                                System.out.println("Voting System: Voting finished");
                                waitingForResult = false;
                            }else {
                                System.out.println("Voting System: Vote received but not settled yet");
                            }
                            receivedBlock = null;
                        } catch (NoSuchAlgorithmException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
