package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;

import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT;
import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_CREATESTANDARDACCOUNT;

/**
 * Offers several account-related methods for use in smart contracts.
 */
public class Account {

    /**
     * Constructs the script hash for the given public key.
     * <p>
     * More precisely, a verification script is produced from the public key and the hash of that script is returned.
     *
     * @param pubKey the public key to get the script hash for.
     * @return the script hash.
     */
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
     * @param pubKeys the public key to get the script hash for.
     * @return the script hash.
     */
    @Instruction(interopService = SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT)
    public static native Hash160 createMultiSigAccount(int m, ECPoint[] pubKeys);

}
