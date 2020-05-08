package DataStructures.Transaction;

public class SpecialTransaction extends Transaction {


    public SpecialTransaction(int outputCount) {
        super(0, outputCount);
    }

    @Override
    public boolean isValidOutputCount() {
        return this.transactionOutputs.length > 0;
    }

    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum < 0;
    }
}
