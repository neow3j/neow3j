package io.neow3j.devpack.framework;

public class Block {

    public final byte[] hash;
    public final int version;
    public final byte[] prevHash;
    public final byte[] merkleRoot;
    public final long timestamp;
    public final long index;
    public final byte[] nextConsensus;
    public final int transactionsCount;

    private Block() {
        hash = new byte[0];
        version = 0;
        prevHash = new byte[0];
        merkleRoot = new byte[0];
        timestamp = 0;
        transactionsCount = 0;
        nextConsensus = new byte[0];
        index = 0;
    }
}
