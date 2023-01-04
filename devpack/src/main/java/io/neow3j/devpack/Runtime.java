package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.TriggerType;
import io.neow3j.script.InteropService;

import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_CHECKWITNESS;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_GASLEFT;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_GETINVOCATIONCOUNTER;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_GETNOTIFICATIONS;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_GETTIME;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_GETTRIGGER;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_LOG;
import static io.neow3j.script.InteropService.SYSTEM_RUNTIME_PLATFORM;

/**
 * Provides a set of general methods for usage in smart contracts.
 */
public class Runtime {

    /**
     * @return the {@link TriggerType} with which the smart contract has been triggered.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_GETTRIGGER)
    public static native byte getTrigger();

    /**
     * @return the information of the platform on which the smart contract is currently executed.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_PLATFORM)
    public static native String getPlatform();

    /**
     * @return the timestamp of the current block in milliseconds since the start of the Unix epoch.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_GETTIME)
    public static native int getTime();

    /**
     * Gets the number of times the current contract has been invoked.
     *
     * @return the number of times the current contract has been invoked.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_GETINVOCATIONCOUNTER)
    public static native int getInvocationCounter();

    /**
     * Gets the amount of GAS left in the current invocation of the contract.
     *
     * @return the amount of GAS left in the current invocation of the contract (in fractions).
     */
    @Instruction(interopService = SYSTEM_RUNTIME_GASLEFT)
    public static native int getGasLeft();

    /**
     * Gets current invocation notifications matching the given sender script hash. The script hash must be 20 bytes
     * long. If it is all zeros, it refers to all existing notifications.
     *
     * @param hash the sender script hash to get the notifications for.
     * @return an array of all notifications matching the given script hash.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_GETNOTIFICATIONS)
    public static native Notification[] getNotifications(Hash160 hash);

    /**
     * Verifies whether the container (e.g. the transactions) calling the contract is signed by the given public key
     * (e.g. an account).
     *
     * @param pubKey the public key to check.
     * @return true if the given public key is the signer of the transaction. False, otherwise.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_CHECKWITNESS)
    public static native boolean checkWitness(ECPoint pubKey);

    /**
     * Verifies whether the container (e.g. the transactions) calling the contract is signed by the given script hash
     * (e.g. an account).
     *
     * @param scriptHash the script hash to check.
     * @return true if the given script hash is the signer of the transaction. False, otherwise.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_CHECKWITNESS)
    public static native boolean checkWitness(Hash160 scriptHash);

    /**
     * Issues a log message, notifying the client that invoked the contract.
     *
     * @param message the message to log.
     */
    @Instruction(interopService = SYSTEM_RUNTIME_LOG)
    public static native void log(String message);

    /**
     * Gets the container that triggered the execution of the current contract.
     * <p>
     * The container of an contract-invoking script is usually a {@link Transaction}. In that case, the return value
     * can be cast to a {@code Transaction}. E.g.: {@code Transaction tx = (Transaction) Runtime.getScriptContainer();}
     *
     * @return the script container.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETSCRIPTCONTAINER)
    public static native Object getScriptContainer();

    /**
     * Gets the script hash of the currently executing contract in little-endian order.
     *
     * @return the script hash of the executing contract in little-endian order.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH)
    public static native Hash160 getExecutingScriptHash();

    /**
     * Gets the script hash of the caller of the contract in little-endian order.
     * <p>
     * If the contact was invoked directly by a transaction, then this returns the hash of that transaction's script.
     * If the contract is called by another contact as part of an invocation chain, then this returns the script hash
     * of the calling contract.
     *
     * @return the caller's script hash in little-endian order.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH)
    public static native Hash160 getCallingScriptHash();

    /**
     * Gets the script hash of the entry context in little-endian order, i.e., the context at the beginning of the
     * contract invocation chain.
     * <p>
     * In case the invocation was induced by a transaction, the hash of that transaction's script is the entry script
     * hash.
     *
     * @return the script hash in little-endian order.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETENTRYSCRIPTHASH)
    public static native Hash160 getEntryScriptHash();

    /**
     * This method loads the provided bytecode script and executes it with the given call flags and arguments.
     * <p>
     * The execution context is limited to read-only ({@link io.neow3j.devpack.constants.CallFlags#ReadOnly})
     * regardless of the provided call flags (i.e., only providing more restrictive call flags will take effect).
     * <p>
     * The provided script is expected to return none or exactly one value. In any case, this method always returns
     * one value, which is {@code null} if the script did not return anything.
     * <p>
     * Note, that this is similar to a contract call, i.e., the script can {@code ABORT} the transaction or throw an
     * exception. Thus, make sure to handle this appropriately.
     *
     * @param script    the script to load into the NeoVM.
     * @param callFlags the execution context to use.
     * @param arguments the arguments to use.
     * @return the return value of the script execution, or null if there was no return value.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_LOADSCRIPT)
    public static native Object loadScript(ByteString script, byte callFlags, Object[] arguments);

    /**
     * Burns the given amount of GAS in the current invocation. The GAS is taken from the amount available to the
     * invocation (system fee). Any overflow is not consumed from the transaction sender's GAS balance.
     *
     * @param gas the amount of GAS to burn (in GAS fractions).
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_BURNGAS)
    public static native void burnGas(int gas);

    /**
     * @return the magic number of the network the contract is deployed on.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETNETWORK)
    public static native int getNetwork();

    /**
     * @return the 128-bit random number generated from the verifiable random function.
     */
    @Instruction(interopService = InteropService.SYSTEM_RUNTIME_GETRANDOM)
    public static native int getRandom();

}
