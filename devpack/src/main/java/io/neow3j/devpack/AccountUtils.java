package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;

import static io.neow3j.devpack.constants.InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT;
import static io.neow3j.devpack.constants.InteropService.SYSTEM_CONTRACT_CREATESTANDARDACCOUNT;

/**
 * Derives account script hashes for use in smart contracts.
 */
public final class AccountUtils {

    private AccountUtils() {
    }

    /**
     * Derives the hash of the standard account verification script for the given public key.
     *
     * @param pubKey the public key used in the verification script.
     * @return the verification script hash.
     * @since 3.25.0
     */
    @Instruction(interopService = SYSTEM_CONTRACT_CREATESTANDARDACCOUNT)
    public static native Hash160 deriveStandardAccountScriptHash(ECPoint pubKey);

    /**
     * Derives the hash of the multisignature verification script for the given public keys and signing threshold.
     *
     * @param m       the number of required signatures.
     * @param pubKeys the public keys used in the verification script.
     * @return the verification script hash.
     * @since 3.25.0
     */
    @Instruction(interopService = SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
    public static native Hash160 deriveMultiSigAccountScriptHash(int m, ECPoint[] pubKeys);

}
