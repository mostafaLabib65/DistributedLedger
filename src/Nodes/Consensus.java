package Nodes;

import DataStructures.Block.Block;
import DataStructures.Ledger.Ledger;
import network.Process;
import network.entities.CommunicationUnit;
import java.util.ArrayList;


public abstract class Consensus implements Runnable {

    @Override
    public void run() {

    }

    public void setParams(ArrayList<Block> blocks, CommunicationUnit cu, Process process, Ledger ledger){

    }
    public void setParams(ArrayList<Block> blocks, CommunicationUnit cu, Process process, Ledger ledger, int numOfParticipants){

    }
    public void StopMiningCurrentBlock(Block block){
    }
}
