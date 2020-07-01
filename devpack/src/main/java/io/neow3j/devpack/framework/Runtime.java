package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_CHECKWITNESS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_GASLEFT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_GETINVOCATIONCOUNTER;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_GETNOTIFICATIONS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_GETTIME;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_GETTRIGGER;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_LOG;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_NOTIFY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_RUNTIME_PLATFORM;

import io.neow3j.devpack.framework.annotations.Syscall;

/**
 * Provides a set of general methods for usage in smart contracts.
 */
public class Runtime {

    /**
     * Gets the trigger type with which the the smart contract has been triggered.
     *
     * @return the trigger type.
     */
    @Syscall(SYSTEM_RUNTIME_GETTRIGGER)
    public static native TriggerType getTrigger();

    /**
     * Gets information of the platform on which the smart contract is currently executed.
     *
     * @return the platform information.
     */
    @Syscall(SYSTEM_RUNTIME_PLATFORM)
    public static native String getPlatform();

    /**
     * Gets the timestamp of the current block.
     *
     * @return the timestamp
     */
    @Syscall(SYSTEM_RUNTIME_GETTIME)
    public static native long getTime();

    /**
     * Gets the call number of the current contract.
     *
     * @return the call number.
     */
    // TODO: For documentation, clarify if this is actually the number of times the contract has
    //  been invoked.
    @Syscall(SYSTEM_RUNTIME_GETINVOCATIONCOUNTER)
    public static native int getInvocationCounter();

    /**
     * Gets the amount of GAS left in the current invocation of the contract.
     * @return the amount in fractions of GAS.
     */
    // TODO: For documentation, make sure that the above description is correct.
    @Syscall(SYSTEM_RUNTIME_GASLEFT)
    public static native long getGasLeft();

    /**
     * Gets current invocation notifications matching the given sender script hash. The script hash
     * must be 20 bytes long. If it is all zeros, it refers to all existing notifications.
     *
     * @return an array of all notifications matching the given script hash.
     */
    @Syscall(SYSTEM_RUNTIME_GETNOTIFICATIONS)
    public static native Notification[] getNotifications(byte[] hash);

    /**
     * Verifies whether the container (e.g. the transactions) calling the contract is signed by the
     * given script hash or public key (e.g. an account).
     *
     * @param scriptHashOrPubkey The script hash or public key to check.
     * @return true if the given script hash is the signer of the transaction. False, otherwise.
     */
    @Syscall(SYSTEM_RUNTIME_CHECKWITNESS)
    public static native boolean checkWitness(byte[] scriptHashOrPubkey);

    /**
     * Issues a notification, notifying the client that invoked the contract.
     * <p>
     * The given state can be a list of any types, e.g. `notify("Notification reason", Blockchain
     * .getHeight()`. The client developer needs to know what types to expect when reading the
     * notification.
     *
     * @param state The state to send with the notification.
     */
    // TODO: For documentation, figure out if each state object leads a separate notification
    //  object.
    @Syscall(SYSTEM_RUNTIME_NOTIFY)
    public static native void notify(Object... state);

    /**
     * Issues a log message, notifying the client that invoked the contract.
     * <p>
     * This is similar to {@link Runtime#notify(Object...)} but restricted to a string message.
     *
     * @param message The message to log.
     */
    @Syscall(SYSTEM_RUNTIME_LOG)
    public static native void log(String message);
}
