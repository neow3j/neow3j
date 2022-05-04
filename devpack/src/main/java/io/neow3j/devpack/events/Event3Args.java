package io.neow3j.devpack.events;

public class Event3Args<T1, T2, T3> implements EventInterface {

    public native void fire(T1 arg1, T2 arg2, T3 arg3);

}
