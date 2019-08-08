package io.neow3j.wallet;

import java.math.BigDecimal;
import java.util.List;

/**
 * An input calculation strategy is used to determine which unspent transaction outputs (UTXOs)
 * should be used in a transaction. It takes a set of UTXOs and an amount, and returns a minimal set
 * of UTXOs necessary to fulfill the amount.
 */
public interface InputCalculationStrategy {

   InputCalculationStrategy DEFAULT_STRATEGY = new LeftToRightInputCalculationStrategy();

    List<Utxo> calculateInputs(List<Utxo> ouputs, BigDecimal requiredAmount);
}
