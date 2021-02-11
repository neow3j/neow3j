package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.neo.Block;
import io.neow3j.devpack.neo.Transaction;

@ContractHash("0x971d69c6dd10ce88e7dfffec1dc603c6125a8764")
public class LedgerContract extends ContractInterface {

    public static native Hash256 currentHash();

    public static native int currentIndex();

    public static native Block getBlock(int index);

    public static native Block getBlock(Hash256 hash);

    public static native Transaction getTransaction(Hash256 hash);

    public static native Transaction getTransactionFromBlock(Hash256 blockHash, int txIndex);

    public static native Transaction getTransactionFromBlock(int blockHeight, int txIndex);

    public static native int getTransactionHeight(Hash256 hash);


}
