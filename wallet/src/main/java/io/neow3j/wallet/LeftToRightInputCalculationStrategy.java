package io.neow3j.wallet;

import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LeftToRightInputCalculationStrategy implements InputCalculationStrategy {

    @Override
    public List<Utxo> calculateInputs(List<Utxo> utxos, BigDecimal requiredAmount) {
        BigDecimal amount = BigDecimal.ZERO;
        ListIterator<Utxo> it = utxos.listIterator();
        List<Utxo> inputs = new ArrayList<>();
        while(amount.compareTo(requiredAmount) < 0 && it.hasNext()) {
            Utxo utxo = it.next();
            amount = amount.add(utxo.getValue());
            inputs.add(utxo);
        }
        if (amount.compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException();
        }
        return inputs;
    }
}
