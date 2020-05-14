package DataStructures.Transaction;

import java.io.Serializable;

public class SpecialTransaction extends Transaction implements Serializable {


    public SpecialTransaction(int outputCount) {
        super(0, outputCount);
    }

    @Override
    public boolean isValidOutputCount() {
        return this.getTransactionOutputs().length > 0;
    }

    @Override
    public boolean validateInputOutputDifference(long sum) {
        return sum >= -625000000;
    }
}
