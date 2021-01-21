package io.neow3j.devpack.system;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.ApiInterface;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.neo.Transaction;

/**
 * Provides access to the caller of a smart contract and other information about the current
 * contract execution.
 */
public class ExecutionEngine {

    /**
     * Gets the container that triggered the execution of the current contract.
     * <p>
     * The container of an contract-invoking script is usually a {@link Transaction}. In that case,
     * the return value can be cast to a <tt>Transaction</tt>. E.g.:
     * <p>
     * <tt>
     * Transaction tx = (Transaction) ExecutionEngine.getScriptContainer();
     * </tt>
     *
     * @return the script container.
     */
    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_GETSCRIPTCONTAINER)
    public static native Transaction getScriptContainer();

    /**
     * Gets the script hash of the currently executing contract.
     *
     * @return the script hash of the executing contract.
     */
    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH)
    public static native Hash160 getExecutingScriptHash();

    /**
     * Gets the script hash of the caller of the contract.
     *
     * @return the caller's script hash.
     */
    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH)
    public static native Hash160 getCallingScriptHash();

    /**
     * Gets the script hash of the entry points of the contract (in the contract invocation chain).
     *
     * @return the script hash.
     */
    // TODO: Clarify what this method does. Docs are just copied from docs.neo.org.
    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_GETENTRYSCRIPTHASH)
    public static native Hash160 getEntryScriptHash();

}
