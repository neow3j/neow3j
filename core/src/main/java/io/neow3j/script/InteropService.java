package io.neow3j.script;

import io.neow3j.crypto.Hash;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

/**
 * Enumerates all the interoperability services that a neo-node should provide to the NeoVM execution environment.
 * These services can be used in smart contract code via the {@link OpCode#SYSCALL} instruction.
 */
public enum InteropService {

    SYSTEM_CRYPTO_CHECKSIG("System.Crypto.CheckSig", 1 << 15),
    SYSTEM_CRYPTO_CHECKMULTISIG("System.Crypto.CheckMultisig", 0),

    SYSTEM_CONTRACT_CALL("System.Contract.Call", 1 << 15),
    SYSTEM_CONTRACT_CALLNATIVE("System.Contract.CallNative", 0),
    SYSTEM_CONTRACT_GETCALLFLAGS("System.Contract.GetCallFlags", 1 << 10),
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT("System.Contract.CreateStandardAccount", SYSTEM_CRYPTO_CHECKSIG.price),
    SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT("System.Contract.CreateMultisigAccount", 0),

    SYSTEM_CONTRACT_NATIVEONPERSIST("System.Contract.NativeOnPersist", 0),
    SYSTEM_CONTRACT_NATIVEPOSTPERSIST("System.Contract.NativePostPersist", 0),

    SYSTEM_ITERATOR_NEXT("System.Iterator.Next", 1 << 15),
    SYSTEM_ITERATOR_VALUE("System.Iterator.Value", 1 << 4),

    SYSTEM_RUNTIME_PLATFORM("System.Runtime.Platform", 1 << 3),
    SYSTEM_RUNTIME_GETTRIGGER("System.Runtime.GetTrigger", 1 << 3),
    SYSTEM_RUNTIME_GETTIME("System.Runtime.GetTime", 1 << 3),
    SYSTEM_RUNTIME_GETSCRIPTCONTAINER("System.Runtime.GetScriptContainer", 1 << 3),
    SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH("System.Runtime.GetExecutingScriptHash", 1 << 4),
    SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH("System.Runtime.GetCallingScriptHash", 1 << 4),
    SYSTEM_RUNTIME_GETENTRYSCRIPTHASH("System.Runtime.GetEntryScriptHash", 1 << 4),
    SYSTEM_RUNTIME_LOADSCRIPT("System.Runtime.LoadScript", 1 << 15),
    SYSTEM_RUNTIME_CHECKWITNESS("System.Runtime.CheckWitness", 1 << 10),
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER("System.Runtime.GetInvocationCounter", 1 << 4),
    SYSTEM_RUNTIME_LOG("System.Runtime.Log", 1 << 15),
    SYSTEM_RUNTIME_NOTIFY("System.Runtime.Notify", 1 << 15),
    SYSTEM_RUNTIME_GETNOTIFICATIONS("System.Runtime.GetNotifications", 1 << 12),
    SYSTEM_RUNTIME_GASLEFT("System.Runtime.GasLeft", 1 << 4),
    SYSTEM_RUNTIME_BURNGAS("System.Runtime.BurnGas", 1 << 4),
    SYSTEM_RUNTIME_GETNETWORK("System.Runtime.GetNetwork", 1 << 3),
    SYSTEM_RUNTIME_GETRANDOM("System.Runtime.GetRandom", 1 << 4),

    SYSTEM_STORAGE_GETCONTEXT("System.Storage.GetContext", 1 << 4),
    SYSTEM_STORAGE_GETREADONLYCONTEXT("System.Storage.GetReadOnlyContext", 1 << 4),
    SYSTEM_STORAGE_ASREADONLY("System.Storage.AsReadOnly", 1 << 4),
    SYSTEM_STORAGE_GET("System.Storage.Get", 1 << 15),
    SYSTEM_STORAGE_FIND("System.Storage.Find", 1 << 15),
    SYSTEM_STORAGE_PUT("System.Storage.Put", 1 << 15),
    SYSTEM_STORAGE_DELETE("System.Storage.Delete", 1 << 15),

    DUMMY("Dummy", 0);

    private final String name;

    private final long price;

    /**
     * Constructs a new interop service code.
     *
     * @param name  the name of the service.
     * @param price the execution GAS price of the code.
     */
    InteropService(String name, long price) {
        this.name = name;
        this.price = price;
    }

    /**
     * @return the name of the interop service code.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return this {@code InteropServiceCode}'s hash (4 bytes) as a hex string.
     */
    public String getHash() {
        byte[] sha256 = Hash.sha256(this.getName().getBytes(StandardCharsets.US_ASCII));
        return Numeric.toHexStringNoPrefix(ArrayUtils.getFirstNBytes(sha256, 4));
    }

    /**
     * Gets the price for executing the service.
     * <p>This is a relative price that is multiplied with the {@code execFeeFactor} for the definitive GAS price.
     *
     * @return the price.
     * @throws UnsupportedOperationException if the {@code InteropServiceCode} does not have a fixed price.
     */
    public long getPrice() {
        if (price == 0) {
            throw new UnsupportedOperationException(
                    format("The price of the interop service %s is not fixed.", this.getName()));
        }
        return this.price;
    }

    @Override
    public String toString() {
        return getName();
    }

}
