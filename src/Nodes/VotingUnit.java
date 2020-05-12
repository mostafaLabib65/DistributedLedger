package Nodes;

import DataStructures.Block.Block;

public class VotingUnit {

    private Block block;
    private int population;
    private int posVotes = 0;
    private int negVotes = 0;
    public VotingUnit(Block block, int population){
        this.block = block;
        this.population = population;
    }

    public int addVote(boolean vote){
        if(vote){
            posVotes++;
        }else {
            negVotes++;
        }
        if(posVotes > population/2){
            return 1;
        }else if(negVotes > population/2){
            return 0;
        }else {
            return -1;
        }
    }

    public Block getBlock(){
        return this.block;
    }
}
