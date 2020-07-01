package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALLEX;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CREATESTANDARDACCOUNT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_DESTROY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_GETCALLFLAGS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_UPDATE;

import io.neow3j.devpack.framework.annotations.Syscall;

/**
 * Represents a Neo smart contract.
 */
public class Contract {

    /**
     * The contract's VM script.
     */
    public final byte[] script;

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
        isPayable = false;
        hasStorage = false;
    }

    @Syscall(SYSTEM_CONTRACT_CALL)
    public static native Object call(byte[] scriptHash, String method, Object[] arguments);

    @Syscall(SYSTEM_CONTRACT_CALLEX)
    public static native Object callEx(byte[] scriptHash, String method, Object[] arguments,
            CallFlags flag);

    @Syscall(SYSTEM_CONTRACT_CREATE)
    public static native Contract create(byte[] script, String manifest);

    @Syscall(SYSTEM_CONTRACT_UPDATE)
    public static native void update(byte[] script, String manifest);

    @Syscall(SYSTEM_CONTRACT_DESTROY)
    public static native void destroy();

    @Syscall(SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();

    @Syscall(SYSTEM_CONTRACT_CREATESTANDARDACCOUNT)
    public static native byte[] createStandardAccount(byte[] pubKey);

}
