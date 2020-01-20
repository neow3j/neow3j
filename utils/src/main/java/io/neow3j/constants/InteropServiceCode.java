package io.neow3j.constants;

import io.neow3j.utils.Numeric;

public enum InteropServiceCode {

    NEO_ACCOUNT_ISSTANDARD("b542794c", 30000),
    NEO_CONTRACT_CREATE("f66ca56e", null), // (Script.Size + Manifest.Size) * GasPerByte
    NEO_CONTRACT_GETSCRIPT("791581b6", 400),
    NEO_CONTRACT_ISPAYABLE("77fd08c8", 400),
    NEO_CONTRACT_UPDATE("0b5eb3e3", null), // (Script.Size + Manifest.Size) * GasPerByte
    NEO_CRYPTO_CHECKMULTISIG("c7c34cba", null), // 1 000 000 * nr of signatures
    NEO_CRYPTO_CHECKSIG("747476aa", 1000000),
    NEO_ENUMERATOR_CONCAT("3eae34cb", 400),
    NEO_ENUMERATOR_CREATE("4eaada58", 400),
    NEO_ENUMERATOR_NEXT("42298fe0", 1000000),
    NEO_ENUMERATOR_VALUE("ab5b018e", 400),
    NEO_HEADER_GETMERKLEROOT("15d6cc1b", 400),
    NEO_HEADER_GETNEXTCONSENSUS("b57e5b55", 400),
    NEO_HEADER_GETVERSION("d0d84bd9", 400),
    NEO_ITERATOR_CONCAT("8e4d5e12", 400),
    NEO_ITERATOR_CREATE("24ef3dbf", 400),
    NEO_ITERATOR_KEY("813425bc", 400),
    NEO_ITERATOR_KEYS("0aef6adf", 400),
    NEO_ITERATOR_VALUES("257e13d9", 400),
    NEO_JSON_DESERIALIZE("0b6ebcb4", 500000),
    NEO_JSON_SERIALIZE("5c2159c7", 100000),
    NEO_NATIVE_DEPLOY("123e7fe8", 0),
    NEO_STORAGE_FIND("881908e9", 1000000),
    NEO_TRANSACTION_GETSCRIPT("5000b5d5", 400),
    NEO_TRANSACTION_GETWITNESSES("f70147d8", 10000),
    NEO_WITNESS_GETVERIFICATIONSCRIPT("efcaaa6d", 400),

    SYSTEM_BLOCK_GETTRANSACTION("362b8107", 400),
    SYSTEM_BLOCK_GETTRANSACTIONCOUNT("14d36a25", 400),
    SYSTEM_BLOCK_GETTRANSACTIONS("c0d9af6f", 10000),
    SYSTEM_BLOCKCHAIN_GETBLOCK("8347922d", 2500000),
    SYSTEM_BLOCKCHAIN_GETCONTRACT("a9c54b41", 1000000),
    SYSTEM_BLOCKCHAIN_GETHEADER("74445bf6", 7000),
    SYSTEM_BLOCKCHAIN_GETHEIGHT("7ef5721f", 400),
    SYSTEM_BLOCKCHAIN_GETTRANSACTION("e6558d48", 1000000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT("4a3288b1", 1000000),
    SYSTEM_CONTRACT_CALL("627d5b52", 1000000),
    SYSTEM_CONTRACT_DESTROY("c69f1df0", 1000000),
    SYSTEM_CRYPTO_VERIFY("de789769", 1000000),
    SYSTEM_EXECUTIONENGINE_GETCALLINGSCRIPTHASH("45995a5c", 400),
    SYSTEM_EXECUTIONENGINE_GETENTRYSCRIPTHASH("1d59e119", 400),
    SYSTEM_EXECUTIONENGINE_GETEXECUTINGSCRIPTHASH("87c3d264", 400),
    SYSTEM_EXECUTIONENGINE_GETSCRIPTCONTAINER("9a1f194a", 250),
    SYSTEM_HEADER_GETHASH("b80639a1", 400),
    SYSTEM_HEADER_GETINDEX("de2e7958", 400),
    SYSTEM_HEADER_GETPREVHASH("fabe7210", 400),
    SYSTEM_HEADER_GETTIMESTAMP("15abc264", 400),
    SYSTEM_RUNTIME_CHECKWITNESS("f827ec8c", 30000),
    SYSTEM_RUNTIME_DESERIALIZE("dfedd7bf", 500000),
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("84271143", 400),
    SYSTEM_RUNTIME_GETNOTIFICATIONS("274335f1", 10000),
    SYSTEM_RUNTIME_GETTIME("b7c38803", 250),
    SYSTEM_RUNTIME_GETTRIGGER("e97d38a0", 250),
    SYSTEM_RUNTIME_LOG("cfe74796", 300000),
    SYSTEM_RUNTIME_NOTIFY("95016f61", 250),
    SYSTEM_RUNTIME_PLATFORM("b279fcf6", 250),
    SYSTEM_RUNTIME_SERIALIZE("ee83bbc8", 100000),
    SYSTEM_STORAGE_DELETE("2f58c5ed", 1000000),
    SYSTEM_STORAGE_GET("925de831", 1000000),
    SYSTEM_STORAGE_GETCONTEXT("9bf667ce", 400),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("f6b46be2", 400),
    SYSTEM_STORAGE_PUT("e63f1884", null), // (Key.Size + Value.Size) * GasPerByte
    SYSTEM_STORAGE_PUTEX("73e19b3a", null), // (Key.Size + Value.Size) * GasPerByte
    SYSTEM_STORAGECONTEXT_ASREADONLY("1abdce13", 400),
    SYSTEM_TRANSACTION_GETHASH("ba9e3027", 400);

    private String code;
    private Long price;

    /**
     * Constructs a new interop service code.
     *
     * @param code  A short hash of the code's name as a little endian hex string.
     * @param price The execution GAS price of the code.
     */
    InteropServiceCode(String code, Integer price) {
        this.code = code;
        if (price != null) {
            this.price = (long) price;
        }
    }

    /**
     * Gets the short hash of the code's name as a little endian hex string.
     *
     * @return the hashed code name
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the short hash of the code's name as a little endian byte array.
     *
     * @return the hashed code name
     */
    public byte[] getCodeBytes() {
        return Numeric.hexStringToByteArray(code);
    }

    public long getPrice() {
        if (this.price == null) {
            throw new UnsupportedOperationException("The Interop Service Code " + name() + " does "
                    + "not have a fixed GAS price. It depends on other variables.");
        }
        return price;
    }

    public static long getCheckMultiSigPrice(int nrOfSignatures) {
        return nrOfSignatures * NEO_CRYPTO_CHECKSIG.price;
    }

    public static long getStoragePrice(int byteSize) {
        return byteSize * NeoConstants.GAS_PER_BYTE;
    }

    @Override
    public String toString() {
        return getCode();
    }
}
