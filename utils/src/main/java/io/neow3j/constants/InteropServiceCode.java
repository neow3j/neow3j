package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

public enum InteropServiceCode {

    SYSTEM_ENUMERATOR_CREATE("System.Enumerator.Create", 400),
    SYSTEM_ENUMERATOR_NEXT("System.Enumerator.Next", 1_000_000),
    SYSTEM_ENUMERATOR_VALUE("System.Enumerator.Value", 400),
    SYSTEM_ENUMERATOR_CONCAT("System.Enumerator.Concat", 400),

    SYSTEM_ITERATOR_CREATE("System.Iterator.Create", 400),
    SYSTEM_ITERATOR_KEY("System.Iterator.Key", 400),
    SYSTEM_ITERATOR_KEYS("System.Iterator.Keys", 400),
    SYSTEM_ITERATOR_VALUES("System.Iterator.Values", 400),
    SYSTEM_ITERATOR_CONCAT("System.Iterator.Concat", 400),

    SYSTEM_RUNTIME_PLATFORM("System.Runtime.Platform", 250),
    SYSTEM_RUNTIME_GETTRIGGER("System.Runtime.GetTrigger", 250),
    SYSTEM_RUNTIME_GETTIME("System.Runtime.GetTime", 250),
    SYSTEM_RUNTIME_GETSCRIPTCONTAINER("System.Runtime.GetScriptContainer", 250),
    SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH("System.Runtime.GetExecutingScriptHash", 400),
    SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH("System.Runtime.GetCallingScriptHash", 400),
    SYSTEM_RUNTIME_GETENTRYSCRIPTHASH("System.Runtime.GetEntryScriptHash", 400),
    SYSTEM_RUNTIME_CHECKWITNESS("System.Runtine.CheckWitness", 30000),
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("System.Runtime.GetInvocationCounter", 400),
    SYSTEM_RUNTIME_LOG("System.Runtime.Log", 1_000_000),
    SYSTEM_RUNTIME_NOTIFY("System.Runtime.Notify", 1_000_000),
    SYSTEM_RUNTIME_GETNOTIFICATIONS("System.Runtime.GetNotifications", null), // dynamic calculation
    SYSTEM_RUNTIME_GASLEFT("System.Runtime.GasLeft", 400),

    SYSTEM_STORAGE_GETCONTEXT("System.Storage.GetContext", 400),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("System.Storage.GetReadOnlyContext", 400),
    SYSTEM_STORAGE_ASREADONLY("System.Storage.AsReadOnly", 400),
    SYSTEM_STORAGE_GET("System.Storage.Get", 1000000),
    SYSTEM_STORAGE_FIND("System.Storage.Find", 1000000),
    SYSTEM_STORAGE_PUT("System.Storage.Put", null), // dynamic calculation
    SYSTEM_STORAGE_PUTEX("System.Storage.PutEx", null), // dynamic calculation
    SYSTEM_STORAGE_DELETE("System.Storage.Delete", 100000),

    SYSTEM_BINARY_SERIALIZE("System.Binary.Serialize", 100000),
    SYSTEM_BINARY_DESERIALIZE("System.Binary.Deserialize", 500000),

    SYSTEM_BLOCKCHAIN_GETHEIGHT("System.Blockchain.GetHeight", 400),
    SYSTEM_BLOCKCHAIN_GETBLOCK("System.Blockchain.GetBlock", 2500000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTION("System.Blockchain.GetTransaction", 1000000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT("System.Blockchain.GetTransactionHeight", 1000000),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK("System.Blockchain.GetTransactionFromBlock", 1000000),
    SYSTEM_BLOCKCHAIN_GETCONTRACT("System.Blockchain.GetContract", 1000000),

    SYSTEM_CONTRACT_CREATE("System.Contract.Create", null), // dynamic calculation
    SYSTEM_CONTRACT_UPDATE("System.Contract.Update", null), // dynamic calculation
    SYSTEM_CONTRACT_DESTROY("System.Contract.Destroy", 1000000),
    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1000000),
    SYSTEM_CONTRACT_CALLEX("System.Contract.CallEx", 1000000),
    SYSTEM_CONTRACT_ISSTANDARD("System.Contract.IsStandard", 30000),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 30000),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", 10000),

    NEO_CRYPTO_ECDSA_SECP256R1_VERIFY("Neo.Crypto.ECDsa.Secp256r1.Verify", 1_000_000),
    NEO_CRYPTO_ECDSA_SECP256K1_VERIFY("Neo.Crypto.ECDsa.Secp256k1.Verify", 1_000_000),
    // The price for check multisig is the price for Secp256r1.Verify times the number of signatures
    NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG("Neo.Crypto.ECDsa.Secp256r1.CheckMultiSig", null),
    // The price for check multisig is the price for Secp256k1.Verify times the number of signatures
    NEO_CRYPTO_ECDSA_SECP256K1_CHECKMULTISIG("Neo.Crypto.ECDsa.Secp256k1.CheckMultiSig", null),

    // Native tokens.
    NEO_NATIVE_TOKENS_NEO("Neo.Native.Tokens.NEO", null),
    NEO_NATIVE_TOKENS_GAS("Neo.Native.Tokens.GAS", null);

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
