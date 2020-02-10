package io.neow3j.utils;

import io.neow3j.constants.NeoConstants;

public class FeeUtils {

    private static int calcPushIntSize(int i) {
        if (i <= 16) {
            return 1;
        }
        // If the number is larger than 16, it is handled like a data array. Depending on the
        // size of the number it can take multiple bytes. Since we cannot have more than 1024
        // accounts participating in a multi-sig address, we will never need more than two
        // bytes for storing the number. Therefore, in any case, only one byte is necessary
        // to encode the data length.
        return 1 + BigIntegers.toLittleEndianByteArray(i).length;
    }


    // TODO: Remove or adapt. Correct version is currently in Invocation class.
//    /**
//     * Calculates the network fee (GAS) required for the verification of a single-account
//     * signature.
//     * <p>
//     * This does not include any fee for byte size.
//     *
//     * @return the fee in fractions of GAS.
//     */
//    public static long calcNetworkFeeForSingleSig() {
//        return OpCode.PUSHBYTES64.getPrice()
//            + OpCode.PUSHBYTES33.getPrice()
//            + InteropServiceCode.NEO_CRYPTO_CHECKSIG.getPrice();
//    }

    // TODO: Remove or adapt. Correct version is currently in Invocation class.
//    /**
//     * Calculates the network fee (GAS) required for the verification of a multi-account signature
//     * with the given signing threshold and number of involved accounts.
//     * <p>
//     * This does not include any fee for byte size.
//     *
//     * @param signingThreshold The signing threshold of the multi-sig account.
//     * @param nrOfAccounts     The number of accounts involved in the multi-sig account.
//     * @return the fee in fractions of GAS.
//     */
//    public static long calcNetworkFeeForMultiSig(int signingThreshold, int nrOfAccounts) {
//        ScriptBuilder sb = new ScriptBuilder();
//        OpCode signingThresholdPushOpCode = OpCode
//            .valueOf(sb.pushInteger(signingThreshold).toArray()[0]);
//        OpCode accountsPushOpCode = OpCode.valueOf(sb.pushInteger(nrOfAccounts).toArray()[0]);
//
//        return OpCode.PUSHBYTES64.getPrice() * signingThreshold
//            + signingThresholdPushOpCode.getPrice()
//            + OpCode.PUSHBYTES33.getPrice() * nrOfAccounts
//            + accountsPushOpCode.getPrice()
//            // TODO: Make sure that using the number of accounts here is correct.
//            + InteropServiceCode.getCheckMultiSigPrice(nrOfAccounts);
//    }

    /**
     * Calculates the network fee (GAS) for a transaction of the given size in bytes.
     *
     * @param size The size in bytes.
     * @return the fee in fractions of GAS.
     */
    public static long calcTransactionSizeFee(int size) {
        return size * NeoConstants.GAS_PER_BYTE;
    }
}
