import Nodes.Consensus.Consensus;
import Nodes.Consensus.POWBlockConsumer;
import Nodes.Miner.Miner;
import Nodes.Miner.POWMiner;

public class MinerMain {

    public static void main(String[] args){
        Consensus consensus = new POWBlockConsumer(2);
        Miner miner = new POWMiner(consensus, 3, "127.0.0.1", Integer.parseInt(args[0]),
                Boolean.parseBoolean(args[1]), 2);
    }
}
