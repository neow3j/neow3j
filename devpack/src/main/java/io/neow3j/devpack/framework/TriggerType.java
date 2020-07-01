package io.neow3j.devpack.framework;

/**
 * The <tt>TriggerType</tt> defines the mode in which a smart contract is invoked. This implies
 * that a smart contract can be invoked in other ways than just the normal application mode.
 */
public enum TriggerType {

    System(0x01),

    /**
     * The verification trigger indicates that the contract is being invoked as a verification
     * function. The verification function can accept multiple parameters, and should return a
     * boolean value that indicates the validity of the transaction or block. The entry point of the
     * contract will be invoked if the contract is triggered with this trigger type. The entry point
     * of the contract must be able to handle this trigger type.
     */
    Verification(0x20),

    /**
     * The application trigger indicates that the contract is being invoked as an application. The
     * application function can accept multiple parameters, change the state of the blockchain, and
     * return any type of value. Any function of the contract can server as the entry point with
     * this trigger type.
     */
    Application(0x40),

    All(System.value | Verification.value | Application.value);

    private byte value;

    private TriggerType(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }
}