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
     * {@link WitnessScope#CUSTOM_GROUPS}.
     */
    public ECPoint[] allowedGroups;

    /**
     * The rules that the witness must meet. Only available when the corresponding signer has a witness scope
     * {@link WitnessScope#WITNESS_RULES}.
     */
    public WitnessRule[] witnessRules;

    private Signer() {
    }

    /**
     * Compares this signer to the given object. The comparison happens by reference only. I.e., if you retrieve the
     * same signer twice, e.g., with {@link io.neow3j.devpack.contracts.LedgerContract#getTransactionSigners(Hash256)},
     * then comparing the two will return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same transaction. False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given signer by value.
     * <p>
     * Note, that when using this implementation, a potential {@link WitnessRuleCondition} can only be compared by
     * reference, since its structure is unknown.
     *
     * @param s another signer to compare to.
     * @return true if all fields of the two contracts are equal. False, otherwise.
     */
    public boolean equals(Signer s) {
        if (this == s) {
            return true;
        }
        if (!account.equals(s.account) || witnessScopes != s.witnessScopes) {
            return false;
        }
        if (allowedContracts != s.allowedContracts) { // equal by reference?
            if (allowedContracts.length != s.allowedContracts.length) {
                return false;
            }
            for (int i = 0; i < allowedContracts.length; i++) {
                if (!allowedContracts[i].equals(s.allowedContracts[i])) {
                    return false;
                }
            }
        }
        if (allowedGroups != s.allowedGroups) { // equal by reference?
            if (allowedGroups.length != s.allowedGroups.length) {
                return false;
            }
            for (int i = 0; i < allowedGroups.length; i++) {
                if (!allowedGroups[i].equals(s.allowedGroups[i])) {
                    return false;
                }
            }
        }
        if (witnessRules != s.witnessRules) { // equal by reference?
            if (witnessRules.length != s.witnessRules.length) {
                return false;
            }
            for (int i = 0; i < witnessRules.length; i++) {
                if (!witnessRules[i].equals(s.witnessRules[i])) {
                    return false;
                }
            }
        }
        return true;
    }

}
