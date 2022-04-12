package io.neow3j.devpack.constants;

/**
 * Indicates the status of the VM.
 */
public class VMState {

    /**
     * Indicates that the execution is in progress or has not yet begun.
     */
    public static final byte None = 0;

    /**
     * Indicates that the execution has been completed successfully.
     */
    public static final byte Halt = 1;

    /**
     * Indicates that the execution has ended, and an exception that cannot be caught is thrown.
     */
    public static final byte Fault = 2;

    /**
     * Indicates that a breakpoint is currently being hit.
     */
    public static final byte Break = 4;

}
