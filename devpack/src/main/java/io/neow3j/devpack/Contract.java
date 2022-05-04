package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.CallFlags;
import io.neow3j.script.OpCode;

import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_CALL;
import static io.neow3j.script.InteropService.SYSTEM_CONTRACT_GETCALLFLAGS;

/**
 * Represents a Neo smart contract and provides several contract-related methods for use in smart contracts.
 */
public class Contract {

    /**
     * The contract's ID. Each contract is assigned an ID at deployment time and is fixed.
     */
    public final int id;

    /**
     * The number of times the contract has been updated.
     */
    public final int updateCounter;

    /**
     * The contract's hash in little-endian order.
     */
    public final Hash160 hash;

    /**
     * The contract's NEF.
     */
    public final ByteString nef;

    /**
     * The contract's manifest in JSON format.
     */
    public final Manifest manifest;

    private Contract() {
        id = 0;
        updateCounter = 0;
        hash = new Hash160(new byte[0]);
        nef = new ByteString("");
        manifest = null;
    }

    /**
     * Makes a call to the {@code method} of the contract with the {@code scriptHash} passing the {@code arguments}.
     * <p>
     * Make sure to pass the script hash in little-endian format, e.g., when you hardcode the hash in your contract
     * code as shown in the following example.
     * <p>
     * {@code Hash160 scriptHash = new Hash160(hexToBytes("cf76e28bd0062c4a478ee35561011319f3cfa4d2"))}
     *
     * @param scriptHash the script hash of the contract to invoke in little-endian format.
     * @param method     the method to call.
     * @param callFlags  the {@link CallFlags} to use for the call.
     * @param arguments  the arguments to hand to the method. If the called method doesn't take any arguments pass an
     *                   empty object array, i.e., {@code new Object[]{}}. Passing null will make the contract fail at
     *                   runtime.
     * @return the value returned by the contract method call.
     */
    @Instruction(interopService = SYSTEM_CONTRACT_CALL)
    public static native Object call(Hash160 scriptHash, String method, byte callFlags, Object[] arguments);

    /**
     * Gets the call flags with which the contract has been called. I.e., use this to know with which call flags your
     * contract is being called.
     *
     * @return the call flags encoded in one byte.
     */
    @Instruction(interopService = SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();

    /**
     * Compares this contract to the given object. The comparison happens by reference only. I.e., if you retrieve
     * the same contract twice, e.g., with
     * {@link io.neow3j.devpack.contracts.ContractManagement#getContract(Hash160)}, then comparing the two will
     * return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same contract. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given contract by value.
     *
     * @param contract another contract to compare this contract to.
     * @return true if all fields of the two contracts are equal. False, otherwise.
     */
    public boolean equals(Contract contract) {
        if (this == contract) {
            return true;
        }
        return id == contract.id &&
                updateCounter == contract.updateCounter &&
                hash.equals(contract.hash) &&
                nef.equals(contract.nef) &&
                manifest.equals(contract.manifest);
    }

}
