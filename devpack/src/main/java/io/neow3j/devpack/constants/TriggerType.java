package io.neow3j.devpack.constants;

/**
 * The {@code TriggerType} defines the mode in which a smart contract is invoked. This implies that
 * a smart contract can be invoked in other ways than just the normal application mode.
 */
public class TriggerType {

    public static final byte SYSTEM = 0x01;

    /**
     * The verification trigger indicates that the contract is being invoked as a verification
     * function. The verification function can accept multiple parameters, and should return a
     * boolean value that indicates the validity of the transaction or block.
     */
    public static final byte VERIFICATION = 0x20;

    /**
     * The application trigger indicates that the contract is being invoked as an application. The
     * application function can accept multiple parameters, change the state of the blockchain, and
     * return any type of value. Any function of the contract can server as the entry point with
     * this trigger type.
     */
    public static final byte APPLICATION = 0x40;

    public static final byte ALL = SYSTEM | VERIFICATION | APPLICATION;
}