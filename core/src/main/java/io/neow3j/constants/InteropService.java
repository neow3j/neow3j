package io.neow3j.constants;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

/**
 * Enumerates all the interoperability services that a neo-node should provide to the neo-vm
 * execution environment. These services can be used in smart contract code via the
 * {@link OpCode#SYSCALL} instruction.
 */
public enum InteropService {

    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1 << 15),
    SYSTEM_CONTRACT_CALLNATIVE("System.Contract.CallNative", 0),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 1 << 10),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", 1 << 8),
    SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT("System.Contract.CreateMultisigAccount", 1 << 8),

    SYSTEM_CONTRACT_NATIVEONPERSIST("System.Contract.NativeOnPersist", 0),
    SYSTEM_CONTRACT_NATIVEPOSTPERSIST("System.Contract.NativePostPersist", 0),

    SYSTEM_CRYPTO_CHECKSIG("System.Crypto.CheckSig", 1 << 15),
    SYSTEM_CRYPTO_CHECKMULTISIG("System.Crypto.CheckMultisig", 0),

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
    SYSTEM_RUNTIME_BURNGAS("System.Runtime.BurnGas", 1 << 4),

    SYSTEM_STORAGE_GETCONTEXT("System.Storage.GetContext", 1 << 4),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("System.Storage.GetReadOnlyContext", 1 << 4),
    SYSTEM_STORAGE_ASREADONLY("System.Storage.AsReadOnly", 1 << 4),
    SYSTEM_STORAGE_GET("System.Storage.Get", 1 << 15),
    SYSTEM_STORAGE_FIND("System.Storage.Find", 1 << 15),
    SYSTEM_STORAGE_PUT("System.Storage.Put", 0),
    SYSTEM_STORAGE_PUTEX("System.Storage.PutEx", 0),
    SYSTEM_STORAGE_DELETE("System.Storage.Delete", 0);

    private final String name;

    private final long price;

    /**
     * Constructs a new interop service code.
     *
     * @param name  The name of the service.
     * @param price The execution GAS price of the code.
     */
    InteropService(String name, long price) {
        this.name = name;
        this.price = price;
    }

    /**
     * Gets the name of the interop service code.
     *
     * @return the name.
     */
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
     * Price for executing the service. This is a relative price that is multiplied with the
     * {@code execFeeFactor} for the definitive GAS price.
     *
     * @return the price.
     * @throws UnsupportedOperationException if the {@code InteropServiceCode} does not have a
     *                                       fixed price.
     */
    public long getPrice() {
        if (price == 0) {
            throw new UnsupportedOperationException("The price of the interop service "
                    + this.getName() + " is not fixed.");
        }
        return this.price;
    }

    @Override
    public String toString() {
        return getName();
    }
}
