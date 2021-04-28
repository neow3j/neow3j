package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_GETCALLFLAGS;

/**
 * Represents a Neo smart contract and provides several contract-related methods for use in smart
 * contracts.
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
     * The contract's hash.
     */
    public final Hash160 hash;

    /**
     * The contract's NEF.
     */
    public final ByteString nef;

    /**
     * The contract's manifest in JSON format.
     */
    public final String manifest;

    private Contract() {
        id = 0;
        updateCounter = 0;
        hash = new Hash160(new byte[0]);
        nef = new ByteString("");
        manifest = "";
    }

    /**
     * Makes a call to the {@code method} of the contract with the {@code scriptHash} passing the
     * {@code arguments}.
     *
     * @param scriptHash The script hash of the contract to invoke.
     * @param method     The method to call.
     * @param callFlags  The {@link CallFlags} to use for the call.
     * @param arguments  The arguments to hand to the method.
     * @return the value returned by the contract method call.
     */
    @Syscall(SYSTEM_CONTRACT_CALL)
    public static native Object call(Hash160 scriptHash, String method, byte callFlags,
            Object[] arguments);

    /**
     * Gets the call flags with which the contract has been called. I.e., use this to know with
     * which call flags your contract is being called.
     *
     * @return the call flags encoded in one byte.
     */
    @Syscall(SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();


    /**
     * Compares this contract to the given object. The comparison happens by reference only. I.e.,
     * if you retrieve the same contract twice, e.g., with
     * {@link io.neow3j.devpack.contracts.ContractManagement#getContract(Hash160)}, then
     * comparing the two will return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same contract. False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given contract by value.
     *
     * @param contract Other contract to compare this contract to.
     * @return True if all fields of the two contracts are equal. False otherwise.
     */
    public boolean equals(Contract contract) {
        if (this == contract) return true;
        return id == contract.id
                && updateCounter == contract.updateCounter
                && hash.equals(contract.hash)
                && nef.equals(contract.nef)
                && manifest.equals(contract.manifest);
    }

}
