package io.neow3j.devpack.framework;

import io.neow3j.devpack.framework.annotations.Syscall;

@Syscall("System.Runtime")
public class Runtime {

    @Syscall("System.Runtime.CheckWitness")
    public static native boolean checkWitness(byte[] hashOrKey);

    @Syscall("System.Runtime.Notify")
    public static native void notify(String s, Object value);
}
