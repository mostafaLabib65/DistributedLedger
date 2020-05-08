package DataStructures.Transaction;

public class NormalTransaction extends Transaction {
    public NormalTransaction(int inputCount, int outputCount) {
        super(inputCount, outputCount);
    }

    @Override
    public boolean isValidOutputCount() {
        return this.transactionOutputs.length <= 2;
    }

    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum >= 0;
    }
}
