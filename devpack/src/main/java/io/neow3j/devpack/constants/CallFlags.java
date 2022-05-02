package io.neow3j.devpack.constants;

/**
 * Represents the operations allowed when a contract is called. This allows you to restrict the actions of a called
 * contract.
 */
public class CallFlags {

    /**
     * No flag is set.
     */
    public static final byte None = 0;

    /**
     * Indicates that the called contract is allowed to read states.
     */
    public static final byte ReadStates = 1;

    /**
     * Indicates that the called contract is allowed to write states.
     */
    public static final byte WriteStates = 1 << 1;

    /**
     * Indicates that the called contract is allowed to call another contract.
     */
    public static final byte AllowCall = 1 << 2;

    /**
     * Indicates that the called contract is allowed to send notifications.
     */
    public static final byte AllowNotify = 1 << 3;

    /**
     * Indicates that the called contract is allowed to read or write states.
     */
    public static final byte States = ReadStates | WriteStates;

    /**
     * Indicates that the called contract is allowed to read states or call another contract.
     */
    public static final byte ReadOnly = ReadStates | AllowCall;

    /**
     * All flags are set.
     */
    public static final byte All = States | AllowCall | AllowNotify;

}
