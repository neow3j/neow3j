package io.neow3j.devpack.framework;

public class Runtime {

    @InteropService("Neo.Runtime.CheckWitness")
    public static native boolean checkWitness(byte[] hashOrKey);

    @InteropService("Neo.Runtime.Notify")
    public static native void notify(String s, Object value);
}
