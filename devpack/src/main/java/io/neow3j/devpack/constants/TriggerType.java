package io.neow3j.devpack.constants;

/**
 * The {@code TriggerType} defines the mode in which a smart contract is invoked. This implies that a smart contract
 * can be invoked in other ways than just the normal application mode.
 */
public class TriggerType {

    /**
     * Indicate that the contract is triggered by the system to execute the OnPersist method of the native contracts.
     */
    public static final byte OnPersist = 0x01;

    /**
     * Indicate that the contract is triggered by the system to execute the PostPersist method of the native contracts.
     */
    public static final byte PostPersist = 0x02;

    /**
     * The verification trigger indicates that the contract is being invoked as a verification function.
     */
    public static final byte Verification = 0x20;

    /**
     * Indicates that the contract is triggered by the execution of a transaction.
     */
    public static final byte Application = 0x40;

    /**
     * The combination of all system triggers.
     */
    public static final byte System = OnPersist | PostPersist;

    /**
     * The combination of all triggers.
     */
    public static final byte All = OnPersist | PostPersist | Verification | Application;

}
