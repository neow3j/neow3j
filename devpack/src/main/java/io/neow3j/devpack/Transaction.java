package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * Represents a transaction and provides transaction-related information. It is returned for example when calling
 * {@link io.neow3j.devpack.contracts.LedgerContract#getTransaction(Hash256)} .
 */
public class Transaction {

    /**
     * The hash of this transaction in little-endian order.
     */
    public final Hash256 hash;

    /**
     * The transaction's version number.
     */
    public final byte version;

    /**
     * A random number assigned to this transaction to make it unique (e.g. to thwart replay attacks).
     */
    public final int nonce;

    /**
     * Script hash of the transaction's sender.
     */
    public final Hash160 sender;

    /**
     * The system fee payed with this transaction. The system fee covers the execution cost of the script contained
     * in a transaction.
     */
    public final int systemFee;

    /**
     * The network fee payed with this transaction. The network fee covers the size cost of the transaction and the
     * execution cost for checking its signatures.
     */
    public final int networkFee;

    /**
     * The block height up to which this transaction is valid as long as it is not included into a block.
     */
    public final int validUntilBlock;

    /**
     * The script contained in this transaction.
     */
    public final ByteString script;

    private Transaction() {
        hash = new Hash256(new byte[0]);
        script = null;
        systemFee = 0;
        version = 0;
        nonce = 0;
        sender = new Hash160(new byte[0]);
        networkFee = 0;
        validUntilBlock = 0;
    }

    /**
     * Compares this transaction to the given object. The comparison happens by reference only. I.e., if you retrieve
     * the same transaction twice, e.g., with
     * {@link io.neow3j.devpack.contracts.LedgerContract#getTransaction(Hash256)}, then comparing the two will return
     * false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same transaction. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given transaction by value.
     *
     * @param tx another transaction to compare to.
     * @return true if all fields of the two contracts are equal. False, otherwise.
     */
    public boolean equals(Transaction tx) {
        if (this == tx) {
            return true;
        }
        return version == tx.version && nonce == tx.nonce && systemFee == tx.systemFee && networkFee == tx.networkFee &&
                validUntilBlock == tx.validUntilBlock && hash.equals(tx.hash) && sender.equals(tx.sender) &&
                script.equals(tx.script);
    }

}
