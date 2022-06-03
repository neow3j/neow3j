package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;
import io.neow3j.transaction.WitnessScope;

/**
 * Represents a signer and provides signer-related information. It is returned for example when calling
 * {@link io.neow3j.devpack.contracts.LedgerContract#getTransactionSigners(Hash256)} .
 */
public class Signer {

    /**
     * The account of the signer.
     */
    public Hash160 account;

    /**
     * The scopes of the witness.
     */
    public byte witnessScopes;

    /**
     * The contracts that are allowed by the witness. Only available when the corresponding signer has a witness scope
     * {@link WitnessScope#CUSTOM_CONTRACTS}.
     */
    public Hash160[] allowedContracts;

    /**
     * The groups that are allowed by the witness. Only available when the corresponding signer has a witness scope
     * {@link io.neow3j.transaction.WitnessScope#CUSTOM_GROUPS}.
     */
    public ECPoint[] allowedGroups;

    /**
     * The groups that are allowed by the witness. Only available when the corresponding signer has a witness scope
     * {@link io.neow3j.transaction.WitnessScope#CUSTOM_GROUPS}.
     */
    public WitnessRule[] witnessRules;

    private Signer() {
    }

    /**
     * Compares this signer to the given object. The comparison happens by reference only. I.e., if you retrieve the
     * same signer twice, e.g., with
     * {@link io.neow3j.devpack.contracts.LedgerContract#getTransactionSigners(Hash256)}, then comparing the two will
     * return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same transaction. False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given signer by value.
     *
     * @param signer other signer to compare to.
     * @return true if all fields of the two contracts are equal. False otherwise.
     */
    public boolean equals(Signer signer) {
        if (this == signer) {
            return true;
        }
        return account == signer.account && allowedContracts == signer.allowedContracts &&
                allowedGroups == signer.allowedGroups;
    }

}
