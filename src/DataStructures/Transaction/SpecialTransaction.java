package DataStructures.Transaction;

public class SpecialTransaction extends Transaction {
    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum < 0;
    }
}
