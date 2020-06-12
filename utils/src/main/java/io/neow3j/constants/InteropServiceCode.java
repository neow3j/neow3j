package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

public enum InteropServiceCode {

    SYSTEM_CONTRACT_CREATE("System.Contract.Create",
            null), // (Script.Size + Manifest.Size) * GasPerByte
    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1000000),

    NEO_CRYPTO_ECDSA_SECP256R1_VERIFY("Neo.Crypto.ECDsa.Secp256r1.Verify", 1_000_000),
    NEO_CRYPTO_ECDSA_SECP256K1_VERIFY("Neo.Crypto.ECDsa.Secp256k1.Verify", 1_000_000),
    // The price for check multisig is the price for Secp256r1.Verify times the number of signatures
    NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG("Neo.Crypto.ECDsa.Secp256r1.CheckMultiSig", null),
    // The price for check multisig is the price for Secp256k1.Verify times the number of signatures
    NEO_CRYPTO_ECDSA_SECP256K1_CHECKMULTISIG("Neo.Crypto.ECDsa.Secp256k1.CheckMultiSig", null),

    // Native tokens. Their script hashes are derived from their names.
    NEO_NATIVE_TOKENS_NEO("Neo.Native.Tokens.NEO", null),
    NEO_NATIVE_TOKENS_GAS("Neo.Native.Tokens.GAS", null);

    // Note that the below commented Interop Service Codes are not up to date.
//    SYSTEM_BINARY_SERIALIZE("ee83bbc8", 100_000),
//    SYSTEM_BINARY_DESERIALIZE("dfedd7bf", 500_000),
//
//    SYSTEM_BLOCKCHAIN_GETHEIGHT("7ef5721f", 400),
//    SYSTEM_BLOCKCHAIN_GETBLOCK("8347922d", 2500000),
//    SYSTEM_BLOCKCHAIN_GETTRANSACTION("e6558d48", 1_000_000),
//    SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT("4a3288b1", 1_000_000),
//    SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK("", 1_000_000),
//    SYSTEM_BLOCKCHAIN_GETCONTRACT("a9c54b41", 1_000_000),

//    NEO_ACCOUNT_ISSTANDARD("b542794c", 30000),
//    NEO_CONTRACT_GETSCRIPT("791581b6", 400),
//    NEO_CONTRACT_ISPAYABLE("77fd08c8", 400),
//    NEO_CONTRACT_UPDATE("0b5eb3e3", null), // (Script.Size + Manifest.Size) * GasPerByte
//    NEO_ENUMERATOR_CONCAT("3eae34cb", 400),
//    NEO_ENUMERATOR_CREATE("4eaada58", 400),
//    NEO_ENUMERATOR_NEXT("42298fe0", 1000000),
//    NEO_ENUMERATOR_VALUE("ab5b018e", 400),
//    NEO_HEADER_GETMERKLEROOT("15d6cc1b", 400),
//    NEO_HEADER_GETNEXTCONSENSUS("b57e5b55", 400),
//    NEO_HEADER_GETVERSION("d0d84bd9", 400),
//    NEO_ITERATOR_CONCAT("8e4d5e12", 400),
//    NEO_ITERATOR_CREATE("24ef3dbf", 400),
//    NEO_ITERATOR_KEY("813425bc", 400),
//    NEO_ITERATOR_KEYS("0aef6adf", 400),
//    NEO_ITERATOR_VALUES("257e13d9", 400),
//    NEO_JSON_DESERIALIZE("0b6ebcb4", 500000),
//    NEO_JSON_SERIALIZE("5c2159c7", 100000),
//    NEO_NATIVE_DEPLOY("123e7fe8", 0),
//    NEO_STORAGE_FIND("881908e9", 1000000),
//    NEO_TRANSACTION_GETSCRIPT("5000b5d5", 400),
//    NEO_TRANSACTION_GETWITNESSES("f70147d8", 10000),
//    NEO_WITNESS_GETVERIFICATIONSCRIPT("efcaaa6d", 400),

//    SYSTEM_BLOCK_GETTRANSACTION("362b8107", 400),
//    SYSTEM_BLOCK_GETTRANSACTIONCOUNT("14d36a25", 400),
//    SYSTEM_BLOCK_GETTRANSACTIONS("c0d9af6f", 10000),
//    SYSTEM_CONTRACT_CALL("627d5b52", 1000000),
//    SYSTEM_CONTRACT_DESTROY("c69f1df0", 1000000),
//    SYSTEM_CRYPTO_VERIFY("de789769", 1000000),
//    SYSTEM_EXECUTIONENGINE_GETCALLINGSCRIPTHASH("45995a5c", 400),
//    SYSTEM_EXECUTIONENGINE_GETENTRYSCRIPTHASH("1d59e119", 400),
//    SYSTEM_EXECUTIONENGINE_GETEXECUTINGSCRIPTHASH("87c3d264", 400),
//    SYSTEM_EXECUTIONENGINE_GETSCRIPTCONTAINER("9a1f194a", 250),
//    SYSTEM_HEADER_GETHASH("b80639a1", 400),
//    SYSTEM_HEADER_GETINDEX("de2e7958", 400),
//    SYSTEM_HEADER_GETPREVHASH("fabe7210", 400),
//    SYSTEM_HEADER_GETTIMESTAMP("15abc264", 400),
//    SYSTEM_RUNTIME_CHECKWITNESS("f827ec8c", 30000),
//    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("84271143", 400),
//    SYSTEM_RUNTIME_GETNOTIFICATIONS("274335f1", 10000),
//    SYSTEM_RUNTIME_GETTIME("b7c38803", 250),
//    SYSTEM_RUNTIME_GETTRIGGER("e97d38a0", 250),
//    SYSTEM_RUNTIME_LOG("cfe74796", 300000),
//    SYSTEM_RUNTIME_NOTIFY("95016f61", 250),
//    SYSTEM_RUNTIME_PLATFORM("b279fcf6", 250),
//    SYSTEM_STORAGE_DELETE("2f58c5ed", 1000000),
//    SYSTEM_STORAGE_GET("925de831", 1000000),
//    SYSTEM_STORAGE_GETCONTEXT("9bf667ce", 400),
//    SYSTEM_STORAGE_GETREADONLYCONTEXT("f6b46be2", 400),
//    SYSTEM_STORAGE_PUT("e63f1884", null), // (Key.Size + Value.Size) * GasPerByte
//    SYSTEM_STORAGE_PUTEX("73e19b3a", null), // (Key.Size + Value.Size) * GasPerByte
//    SYSTEM_STORAGECONTEXT_ASREADONLY("1abdce13", 400),
//    SYSTEM_TRANSACTION_GETHASH("ba9e3027", 400);

    /* The service's name */
    private String name;
    /* Price in fractions of GAS for executing the service. */
    private Long price;

    /**
     * Constructs a new interop service code.
     *
     * @param name  The name of the service.
     * @param price The execution GAS price of the code.
     */
    InteropServiceCode(String name, Integer price) {
        this.name = name;
        if (price != null) {
            this.price = (long) price;
        }
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets this <tt>InteropServiceCode</tt>'s hash (4 bytes) as a hex string.
     *
     * @return the hash.
     */
    public String getHash() {
        byte[] sha256 = Hash.sha256(this.getName().getBytes(StandardCharsets.US_ASCII));
        return Numeric.toHexStringNoPrefix(ArrayUtils.getFirstNBytes(sha256, 4));
    }

    /**
     * Gets the hash (4 bytes) of the given <tt>InteropServiceCode</tt>'s name.
     *
     * @return the hash.
     */
    public static byte[] getInteropCodeHash(String interopCodeName) {
        byte[] sha256 = Hash.sha256(interopCodeName.getBytes(StandardCharsets.US_ASCII));
        return ArrayUtils.getFirstNBytes(sha256, 4);
    }


    /**
     * Get the price in fractions of GAS of this interop service.
     * <p>
     * For some interop service the price depends on an additional parameter. In that case use
     * {@link InteropServiceCode#getPrice(int)}.
     *
     * @return the price
     * @throws UnsupportedOperationException if this interop service has a dynamic price.
     */
    public long getPrice() {
        switch (this) {
            case SYSTEM_CONTRACT_CREATE:
                throw new UnsupportedOperationException("The price of the interop service "
                        + "System.Contract.Create is not fixed but depends on the contract's "
                        + "script and manifest size.");
            case NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG:
            case NEO_CRYPTO_ECDSA_SECP256K1_CHECKMULTISIG:
                throw new UnsupportedOperationException("The price of the interop service "
                        + this.getName() + " is not fixed but depends on the number of "
                        + "signatures.");
            default:
                return this.price;
        }
    }

    /**
     * Get the price in fractions of GAS of this interop service dependent on the given parameter.
     * <p>
     * If the interop service has a fixed price, the parameter is simply ignored and the fixed price
     * is returned.
     *
     * @return the price
     */
    public long getPrice(int param) {
        if (this.price != null) {
            return this.price;
        }
        switch (this) {
            case SYSTEM_CONTRACT_CREATE:
                return param * NeoConstants.GAS_PER_BYTE;
            case NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG:
                return param * NEO_CRYPTO_ECDSA_SECP256R1_VERIFY.price;
            case NEO_CRYPTO_ECDSA_SECP256K1_CHECKMULTISIG:
                return param * NEO_CRYPTO_ECDSA_SECP256K1_VERIFY.price;
            default:
                throw new UnsupportedOperationException("The price for " + this.toString() + " is "
                        + "not defined.");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
