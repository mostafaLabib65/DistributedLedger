package Nodes.MinerUtils;

import DataStructures.Block.Block;

import static Nodes.MinerUtils.Configs.*;

public class VotingUnit {

    private Block block;
    private int population;
    private int posVotes = 0;
    private int negVotes = 0;
    public VotingUnit(Block block, int population){
        this.block = block;
        this.population = population;
    }

    public Configs addVote(boolean vote){
        System.out.println("Voting unite: " + vote);
        if(vote){
            posVotes++;
        }else {
            negVotes++;
        }
        if(posVotes > population/2){
            return ACCEPTED;
        }else if(negVotes >= population/2){
            return REJECTED;
        }else {
            return NOT_SETTELED;
        }
    }

    public Block getBlock(){
        return this.block;
    }
}
