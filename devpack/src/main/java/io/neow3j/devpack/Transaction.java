package io.neow3j.devpack;

/**
 * Represents a transaction and provides transaction-related information. It is returned for example
 * when calling {@link io.neow3j.devpack.contracts.LedgerContract#getTransaction(Hash256)} .
 */
public class Transaction implements ApiInterface {

    /**
     * The hash of the transaction.
     */
    public final Hash256 hash;

    /**
     * The transaction's version number.
     */
    public final byte version;

    /**
     * A random number assigned to this transaction to make it unique (e.g. to thwart replay
     * attacks).
     */
    public final int nonce;

    /**
     * Script hash of the transaction's sender.
     */
    public final Hash160 sender;

    /**
     * The system fee payed with this transaction. The system fee covers the execution cost of
     * the script contained in a transaction.
     */
    public final int systemFee;

    /**
     * The network fee payed with this transaction. The network fee covers the size cost of the
     * transaction and the execution cost for checking its signatures.
     */
    public final int networkFee;

    /**
     * The block height up to which this transaction is valid as long as it is not included into
     * a block.
     */
    public final int validUntilBlock;

    /**
     * The script contained in this transaction.
     */
    public final byte[] script;

    private Transaction() {
        hash = new Hash256(new byte[0]);
        script = new byte[0];
        systemFee = 0;
        version = 0;
        nonce = 0;
        sender = new Hash160(new byte[0]);
        networkFee = 0;
        validUntilBlock = 0;
    }
}