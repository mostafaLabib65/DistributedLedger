package DataStructures.Transaction;

public class NormalTransaction extends Transaction {
    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum >= 0;
    }
}
