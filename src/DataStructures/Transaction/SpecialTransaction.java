package DataStructures.Transaction;

public class SpecialTransaction extends Transaction {

    public SpecialTransaction(int inputCount, int outputCount) {
        super(inputCount, outputCount);
    }

    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum < 0;
    }
}
