package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALLEX;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_DESTROY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_GETCALLFLAGS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_UPDATE;

import io.neow3j.devpack.annotations.Syscall;

/**
 * Represents a Neo smart contract and provides several contract-related methods for use in smart
 * contracts.
 */
public class Contract {

    /**
     * The contract's VM script.
     */
    public final byte[] script;

    /**
     * The contract's manifest.
     */
    // TODO: Clarify what the format of the manifest string is and add that info to the
    //  documentation.
    public final String manifest;

    /**
     * States if this contract needs/has storage.
     */
    public final boolean hasStorage;

    /**
     * States if tokens can be sent to this contract.
     */
    public final boolean isPayable;

    private Contract() {
        script = new byte[0];
        manifest = null;
        isPayable = false;
        hasStorage = false;
    }

    /**
     * Makes a call to the given method of the contract with the given script hash with the given
     * arguments.
     *
     * @param scriptHash The script hash of the contract to invoke.
     * @param method     The method to call.
     * @param arguments  The arguments to hand to the method.
     * @return the value returned by the contract method call.
     */
    @Syscall(SYSTEM_CONTRACT_CALL)
    public static native Object call(byte[] scriptHash, String method, Object... arguments);

    /**
     * Makes a call to the given method of the contract with the given script hash with the given
     * arguments and {@link CallFlags}.
     *
     * @param scriptHash The script hash of the contract to invoke.
     * @param method     The method to call.
     * @param arguments  The arguments to hand to the method.
     * @param callFlag   The {@link CallFlags} to apply to the contract call.
     * @return the value returned by the contract method call.
     */
    @Syscall(SYSTEM_CONTRACT_CALLEX)
    public static native Object call(byte[] scriptHash, String method, byte callFlag,
            Object... arguments);

    /**
     * Deploys a new contract with the given script and manifest.
     *
     * @param script   The script of the contract to deploy.
     * @param manifest The manifest of the contract to deploy.
     * @return The deployed <tt>Contract</tt>.
     */
    @Syscall(SYSTEM_CONTRACT_CREATE)
    public static native Contract create(byte[] script, String manifest);


    /**
     * Migrates or updates the smart contract in which this method is called.
     * <p>
     * This method and {@link Contract#create(byte[], String)} function the same if no migration of
     * the contract's persistent storage is required. If storage migration is required then this
     * method also includes this migration.
     *
     * @param script   The updated script of the contract.
     * @param manifest The updated manifest of the contract.
     */
    // TODO: Clarify how the migration of the storage works and document it here.
    @Syscall(SYSTEM_CONTRACT_UPDATE)
    public static native void update(byte[] script, String manifest);


    /**
     * Destroys the smart contract in which this method is called.
     * <p>
     * A deployed smart contract cannot be destroyed from the outside. Thus, if the contract should
     * be destroyable, the logic has to be written into the contract during development.
     * <p>
     * When the contract is destroyed, its storage area is destroyed as well. If the contract is
     * moved, the contents in the old storage area are copied to the new contract storage area.
     */
    // TODO: Clarify what 'moving' a smart contract means and document it here.
    @Syscall(SYSTEM_CONTRACT_DESTROY)
    public static native void destroy();

    /**
     * Gets the call flags with which the contract has been called.
     *
     * @return the call flags encoded in one byte.
     */
    @Syscall(SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();

}
