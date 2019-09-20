package io.neow3j.utils;

import io.neow3j.constants.NeoConstants;

import java.math.BigDecimal;

public class TransactionUtils {

    /**
     * <p>Calculates the necessary network fee for the given transaction size in bytes.</p>
     * <br>
     * <p>For information on how the network fee is calculated with respect to transaction size,
     * consult the NEO <a href="https://neo.org/blog/details/4148">documentation.</a></p>
     *
     * @param transactionSize The transaction size in bytes.
     * @return the network fee that needs to be added to the transaction.
     */
    public static BigDecimal calcNecessaryNetworkFee(int transactionSize) {
        if (transactionSize > NeoConstants.MAX_FREE_TRANSACTION_SIZE) {
            int chargeableSize = transactionSize - NeoConstants.MAX_FREE_TRANSACTION_SIZE;
            return NeoConstants.FEE_PER_EXTRA_BYTE
                    .multiply(new BigDecimal(chargeableSize))
                    .add(NeoConstants.PRIORITY_THRESHOLD_FEE);
        } else {
            return BigDecimal.ZERO;
        }
    }

}
