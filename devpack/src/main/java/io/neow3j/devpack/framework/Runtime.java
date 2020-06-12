package io.neow3j.devpack.framework;

@Syscall("Neo.Runtime")
public class Runtime {

    @Syscall("Neo.Runtime.CheckWitness")
    public static native boolean checkWitness(byte[] hashOrKey);

    @Syscall("Neo.Runtime.Notify")
    public static native void notify(String s, Object value);
}
