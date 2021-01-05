package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALLEX;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_GETCALLFLAGS;

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
     * The contract's manifest in JSON format.
     */
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
     * Gets the call flags with which the contract has been called.
     *
     * @return the call flags encoded in one byte.
     */
    @Syscall(SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();

}
