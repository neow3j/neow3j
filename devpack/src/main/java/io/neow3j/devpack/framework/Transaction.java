package io.neow3j.devpack.framework;

public class Transaction {

    public final byte[] hash;
    public final byte version;
    public final long nonce;
    public final byte[] sender;
    public final long systemFee;
    public final long networkFee;
    public final long validUntilBlock;
    public final byte[] script;

    private Transaction() {
        hash = new byte[0];
        script = new byte[0];
        systemFee = 0;
        version = 0;
        nonce = 0;
        sender = new byte[0];
        networkFee = 0;
        validUntilBlock = 0;
    }
}
