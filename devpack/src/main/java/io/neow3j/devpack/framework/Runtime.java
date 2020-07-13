package io.neow3j.devpack.framework;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.framework.annotations.Syscall;

public class Runtime {

    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_CHECKWITNESS)
    public static native boolean checkWitness(byte[] hashOrKey);

    @Syscall(InteropServiceCode.SYSTEM_RUNTIME_NOTIFY)
    public static native void notify(String s, Object value);
}
