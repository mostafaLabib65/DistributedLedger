import Nodes.Consensus.BFTBlockConsumer;
import Nodes.Consensus.Consensus;
import Nodes.Miner.BFTMiner;
import Nodes.Miner.Miner;

public class MinerMain {

    public static void main(String[] args){
//        Consensus consensus = new POWBlockConsumer(2);
//        Miner miner = new POWMiner(consensus, 3, "127.0.0.1", Integer.parseInt(args[0]),
//                Boolean.parseBoolean(args[1]), 4);

        Consensus consensus = new BFTBlockConsumer();
        Miner miner = new BFTMiner(consensus, 4, "127.0.0.1", Integer.parseInt(args[0]),
                Boolean.parseBoolean(args[1]), 4);
    }
}
