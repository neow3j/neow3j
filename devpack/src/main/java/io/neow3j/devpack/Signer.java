package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * Represents a transaction and provides transaction-related information. It is returned for example
 * when calling {@link io.neow3j.devpack.contracts.LedgerContract#getTransaction(Hash256)} .
 */
public class Signer {

    /**
     * The account of the signer.
     */
    public Hash160 Account;

//    /**
//     * The scopes of the witness.
//     */
//    public WitnessScope Scopes;

    /**
     * The contracts that are allowed by the witness. Only available when the corresponding signer has a witness scope
     * {@link io.neow3j.transaction.WitnessScope#CUSTOM_CONTRACTS}.
     */
    public Hash160[] AllowedContracts;

    /**
     * The groups that are allowed by the witness. Only available when the corresponding signer has a witness scope
     * {@link io.neow3j.transaction.WitnessScope#CUSTOM_GROUPS}.
     */
    public ECPoint[] AllowedGroups;

    private Signer() {
        this.Account = new Hash160(new byte[0]);
        this.AllowedContracts = new Hash160[0];
        this.AllowedGroups = new ECPoint[0];
    }

    /**
     * Compares this signer to the given object. The comparison happens by reference only.
     * I.e., if you retrieve the same signer twice, e.g., with
     * {@link io.neow3j.devpack.contracts.LedgerContract#getTransactionSigners(Hash256)}, then comparing
     * the two will return false.
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
     * @param signer Other signer to compare to.
     * @return true if all fields of the two contracts are equal. False otherwise.
     */
    public boolean equals(Signer signer) {
        if (this == signer) {
            return true;
        }
        return Account == signer.Account && AllowedContracts == signer.AllowedContracts &&
                AllowedGroups == signer.AllowedGroups;
    }

}
