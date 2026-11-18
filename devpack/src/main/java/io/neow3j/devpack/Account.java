package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;

import static io.neow3j.devpack.constants.InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT;
import static io.neow3j.devpack.constants.InteropService.SYSTEM_CONTRACT_CREATESTANDARDACCOUNT;

/**
 * Derives account script hashes for use in smart contracts.
 *
 * @deprecated Use {@link AccountUtils}.
 */
@Deprecated
public class Account {

    // Keep these annotations so existing callers still compile directly to the same syscalls.

    /**
     * Constructs the script hash for the given public key.
     * <p>
     * More precisely, a verification script is produced from the public key and the hash of that script is returned.
     *
     * @param pubKey the public key to get the script hash for.
     * @return the script hash.
     * @deprecated Use {@link AccountUtils#deriveStandardAccountScriptHash(ECPoint)}.
     */
    @Deprecated
    @Instruction(interopService = SYSTEM_CONTRACT_CREATESTANDARDACCOUNT)
    public static native Hash160 createStandardAccount(ECPoint pubKey);

    /**
     * Constructs the script hash for the multi-sig account with the given public keys and the signing threshold
     * {@code m}.
     * <p>
     * More precisely, a verification script is produced from the public keys and the signing threshold, and the hash
     * of that script is returned.
     *
     * @param m       the signing threshold.
     * @param pubKeys the public keys to get the script hash for.
     * @return the script hash.
     * @deprecated Use {@link AccountUtils#deriveMultiSigAccountScriptHash(int, ECPoint[])}.
     */
    @Deprecated
    @Instruction(interopService = SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
    public static native Hash160 createMultiSigAccount(int m, ECPoint[] pubKeys);

}
