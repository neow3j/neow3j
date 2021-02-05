package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

public enum InteropServiceCode {

    SYSTEM_BINARY_SERIALIZE("System.Binary.Serialize", 1 << 12),
    SYSTEM_BINARY_DESERIALIZE("System.Binary.Deserialize", 1 << 14),
    SYSTEM_BINARY_BASE64ENCODE("System.Binary.Base64Encode", 1 << 12),
    SYSTEM_BINARY_BASE64DECODE("System.Binary.Base64Decode", 1 << 12),
    SYSTEM_BINARY_BASE58ENCODE("System.Binary.Base58Encode", 1 << 12),
    SYSTEM_BINARY_BASE58DECODE("System.Binary.Base58Decode", 1 << 12),
    SYSTEM_BINARY_ITOA("System.Binary.Itoa", 1 << 12),
    SYSTEM_BINARY_ATOI("System.Binary.Atoi", 1 << 12),

    SYSTEM_BLOCKCHAIN_GETHEIGHT("System.Blockchain.GetHeight", 1 << 4),
    SYSTEM_BLOCKCHAIN_GETBLOCK("System.Blockchain.GetBlock", 1 << 16),
    SYSTEM_BLOCKCHAIN_GETTRANSACTION("System.Blockchain.GetTransaction", 1 << 15),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT("System.Blockchain.GetTransactionHeight", 1 << 15),
    SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK("System.Blockchain.GetTransactionFromBlock", 1 << 15),

    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1 << 15),
    SYSTEM_CONTRACT_CALLNATIVE("System.Contract.CallNative", 0),
    SYSTEM_CONTRACT_ISSTANDARD("System.Contract.IsStandard", 1 << 10),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 1 << 10),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", 1 << 8),
    SYSTEM_CONTRACT_NATIVEONPERSIST("System.Contract.NativeOnPersist", 0),
    SYSTEM_CONTRACT_NATIVEPOSTPERSIST("System.Contract.NativePostPersist", 0),

    NEO_CRYPTO_RIPEMD160("Neo.Crypto.RIPEMD160", 1 << 15),
    NEO_CRYPTO_SHA256("Neo.Crypto.SHA256", 1 << 15),
    NEO_CRYPTO_VERIFYWITHECDSASECP256R1("Neo.Crypto.VerifyWithECDsaSecp256r1", 1 << 15),
    NEO_CRYPTO_VERIFYWITHECDSASECP256K1("Neo.Crypto.VerifyWithECDsaSecp256k1", 1 << 15),
    // The price for check multisig is the price for Secp256r1.Verify times the number of signatures
    NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1("Neo.Crypto.CheckMultisigWithECDsaSecp256r1", 0),
    // The price for check multisig is the price for Secp256k1.Verify times the number of signatures
    NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1("Neo.Crypto.CheckMultisigWithECDsaSecp256k1", 0),

    SYSTEM_ENUMERATOR_CREATE("System.Enumerator.Create", 1 << 4),
    SYSTEM_ENUMERATOR_NEXT("System.Enumerator.Next", 1 << 15),
    SYSTEM_ENUMERATOR_VALUE("System.Enumerator.Value", 1 << 4),
    SYSTEM_ENUMERATOR_CONCAT("System.Enumerator.Concat", 1 << 4),

    SYSTEM_ITERATOR_CREATE("System.Iterator.Create", 1 << 4),
    SYSTEM_ITERATOR_KEY("System.Iterator.Key", 1 << 4),
    SYSTEM_ITERATOR_KEYS("System.Iterator.Keys", 1 << 4),
    SYSTEM_ITERATOR_VALUES("System.Iterator.Values", 1 << 4),
    SYSTEM_ITERATOR_CONCAT("System.Iterator.Concat", 1 << 4),

    SYSTEM_JSON_SERIALIZE("System.Json.Serialize", 1 << 12),
    SYSTEM_JSON_DESERIALIZE("System.Json.Deserialize", 1 << 14),

    SYSTEM_RUNTIME_PLATFORM("System.Runtime.Platform", 1 << 3),
    SYSTEM_RUNTIME_GETTRIGGER("System.Runtime.GetTrigger", 1 << 3),
    SYSTEM_RUNTIME_GETTIME("System.Runtime.GetTime", 1 << 3),
    SYSTEM_RUNTIME_GETSCRIPTCONTAINER("System.Runtime.GetScriptContainer", 1 << 3),
    SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH("System.Runtime.GetExecutingScriptHash", 1 << 4),
    SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH("System.Runtime.GetCallingScriptHash", 1 << 4),
    SYSTEM_RUNTIME_GETENTRYSCRIPTHASH("System.Runtime.GetEntryScriptHash", 1 << 4),
    SYSTEM_RUNTIME_CHECKWITNESS("System.Runtime.CheckWitness", 1 << 10),
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("System.Runtime.GetInvocationCounter", 1 << 4),
    SYSTEM_RUNTIME_LOG("System.Runtime.Log", 1 << 15),
    SYSTEM_RUNTIME_NOTIFY("System.Runtime.Notify", 1 << 15),
    SYSTEM_RUNTIME_GETNOTIFICATIONS("System.Runtime.GetNotifications", 1 << 8),
    SYSTEM_RUNTIME_GASLEFT("System.Runtime.GasLeft", 1 << 4),

    SYSTEM_STORAGE_GETCONTEXT("System.Storage.GetContext", 1 << 4),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("System.Storage.GetReadOnlyContext", 1 << 4),
    SYSTEM_STORAGE_ASREADONLY("System.Storage.AsReadOnly", 1 << 4),
    SYSTEM_STORAGE_GET("System.Storage.Get", 1 << 15),
    SYSTEM_STORAGE_FIND("System.Storage.Find", 1 << 15),
    SYSTEM_STORAGE_PUT("System.Storage.Put", 0),
    SYSTEM_STORAGE_PUTEX("System.Storage.PutEx", 0),
    SYSTEM_STORAGE_DELETE("System.Storage.Delete", 0);

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
     * Gets this {@code InteropServiceCode}'s hash (4 bytes) as a hex string.
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
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1:
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1:
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
     * @param param The parameter representing the interop service code
     * @return the price
     */
    public long getPrice(int param) {
        if (this.price != null) {
            return this.price;
        }
        switch (this) {
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1:
                return param * NEO_CRYPTO_VERIFYWITHECDSASECP256R1.price;
            case NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1:
                return param * NEO_CRYPTO_VERIFYWITHECDSASECP256K1.price;
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
