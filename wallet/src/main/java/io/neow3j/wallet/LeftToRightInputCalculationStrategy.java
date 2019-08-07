package io.neow3j.wallet;

import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LeftToRightInputCalculationStrategy implements InputCalculationStrategy {

    @Override
    public List<Utxo> calculateInputs(List<Utxo> utxos, BigDecimal requiredAmount) {
        if (utxos.isEmpty()) {
            throw new IllegalArgumentException("No unspent transaction outputs where available " +
                    "to cover the required amount (" + requiredAmount.toPlainString() + ").");
        }
        if (utxos.size() > 1) {
            String assetId = utxos.get(0).getAssetId();
            if (utxos.stream().anyMatch(u -> !u.getAssetId().equals(assetId))) {
                throw new IllegalArgumentException("The given unspent transaction outputs where " +
                        "of different asset types.");
            }
        }
        BigDecimal amount = BigDecimal.ZERO;
        ListIterator<Utxo> it = utxos.listIterator();
        List<Utxo> inputs = new ArrayList<>();
        while (amount.compareTo(requiredAmount) < 0 && it.hasNext()) {
            Utxo utxo = it.next();
            amount = amount.add(utxo.getValue());
            inputs.add(utxo);
        }
        if (amount.compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException("Couldn't cover the required amount (" +
                    requiredAmount.toPlainString() + ") with the available unspent transaction " +
                    "outputs for asset with hash " + utxos.get(0).getAssetId() + ".");
        }
        return inputs;
    }
}
