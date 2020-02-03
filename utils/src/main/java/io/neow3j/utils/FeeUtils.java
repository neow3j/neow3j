package io.neow3j.utils;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;

public class FeeUtils {

    // 0x21 + 33 bytes pubKey + 0x68 + 0x747476aa
//    public static final int SINGLE_ACCOUNT_VERIFICATION_SCRIPT_SIZE = 1 + 33 + 1 + 4;

//    private static int calcVerificationScriptSize(int nrOfAccounts, int signingThreshold) {
//        if (nrOfAccounts == 1) {
//        }
//        if (nrOfAccounts > 1
//            && nrOfAccounts <= MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT
//            && signingThreshold > 2
//            && nrOfAccounts < signingThreshold
//        ) {
//            // pushInt(signingThreshold) + 0x21 + 33 bytes pubKey_1 + ... + 0x21 + 33 bytes
//            pubKey_m
//            //  + pushInt(nrOfAccounts) + 0x68 + 0xc7c34cba
//            return calcPushIntSize(signingThreshold) // PUSH statement for signing threshold.
//                + nrOfAccounts * (1 + 33) // Size and public keys of all accounts.
//                + calcPushIntSize(nrOfAccounts) // PUSH statement for number of accounts.
//                + 1 // 0x68
//                + 4; // 0xc7c34cba
//        }
//        throw new IllegalArgumentException("The number of accounts must be larger than zero and "
//            + "not larger than " + MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT + ", the signing
//            threshold "
//            + "must be larger than one, and the signing threshold cannot be larger than the
//            number "
//            + "of accounts.");
//    }
//
//    public static int calcInvocationScriptSize(int nrOfSignatures) {
//        // 0x40 + 64-byte signature
//        return nrOfSignatures * (1 + NeoConstants.SIGNATURE_SIZE_BYTES);
//    }

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

    /**
     * Calculates the network fee (GAS) required for the verification of a single-account
     * signature.
     * <p>
     * This does not include any fee for byte size.
     *
     * @return the fee in fractions of GAS.
     */
    public static long calcNetworkFeeForSingleSig() {
        return OpCode.PUSHBYTES64.getPrice()
            + OpCode.PUSHBYTES33.getPrice()
            + InteropServiceCode.NEO_CRYPTO_CHECKSIG.getPrice();
    }

    /**
     * Calculates the network fee (GAS) required for the verification of a multi-account signature
     * with the given signing threshold and number of involved accounts.
     * <p>
     * This does not include any fee for byte size.
     *
     * @param signingThreshold The signing threshold of the multi-sig account.
     * @param nrOfAccounts     The number of accounts involved in the multi-sig account.
     * @return the fee in fractions of GAS.
     */
    public static long calcNetworkFeeForMultiSig(int signingThreshold, int nrOfAccounts) {
        ScriptBuilder sb = new ScriptBuilder();
        OpCode signingThresholdPushOpCode = OpCode
            .valueOf(sb.pushInteger(signingThreshold).toArray()[0]);
        OpCode accountsPushOpCode = OpCode.valueOf(sb.pushInteger(nrOfAccounts).toArray()[0]);

        return OpCode.PUSHBYTES64.getPrice() * signingThreshold
            + signingThresholdPushOpCode.getPrice()
            + OpCode.PUSHBYTES33.getPrice() * nrOfAccounts
            + accountsPushOpCode.getPrice()
            // TODO: Make sure that using the number of accounts here is correct.
            + InteropServiceCode.getCheckMultiSigPrice(nrOfAccounts);
    }

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
