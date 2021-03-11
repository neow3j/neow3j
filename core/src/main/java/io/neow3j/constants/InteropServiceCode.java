package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.charset.StandardCharsets;

public enum InteropServiceCode {

    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1 << 15),
    SYSTEM_CONTRACT_CALLNATIVE("System.Contract.CallNative", 0),
    SYSTEM_CONTRACT_ISSTANDARD("System.Contract.IsStandard", 1 << 10),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 1 << 10),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", 1 << 8),
    SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT("System.Contract.CreateMultisigAccount", 1 << 8),

    SYSTEM_CONTRACT_NATIVEONPERSIST("System.Contract.NativeOnPersist", 0),
    SYSTEM_CONTRACT_NATIVEPOSTPERSIST("System.Contract.NativePostPersist", 0),

    NEO_CRYPTO_CHECKSIG("Neo.Crypto.CheckSig", 1 << 15),
    NEO_CRYPTO_CHECKMULTISIG("Neo.Crypto.CheckMultiSig", 0),

    SYSTEM_ITERATOR_CREATE("System.Iterator.Create", 1 << 4),
    SYSTEM_ITERATOR_NEXT("System.Iterator.Next", 1 << 15),
    SYSTEM_ITERATOR_VALUE("System.Iterator.Value", 1 << 4),

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
     *
     * @return the price
     * @throws UnsupportedOperationException if this interop service has a dynamic price.
     */
    public long getPrice() {
        if (this == NEO_CRYPTO_CHECKMULTISIG) {
            throw new UnsupportedOperationException("The price of the interop service "
                    + this.getName() + " is not fixed but depends on the number of signatures.");
        }
        return this.price;
    }

    @Override
    public String toString() {
        return getName();
    }
}
