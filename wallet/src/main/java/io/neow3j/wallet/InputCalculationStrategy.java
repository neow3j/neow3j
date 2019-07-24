package io.neow3j.wallet;

import java.math.BigDecimal;
import java.util.List;

public interface InputCalculationStrategy {

   InputCalculationStrategy DEFAULT_INPUT_CALCULATION_STRATEGY = new LeftToRightInputCalculationStrategy();

    List<Utxo> calculateInputs(List<Utxo> ouputs, BigDecimal requiredAmount);
}
