package io.neow3j.devpack.events;

public class Event2Args<T1, T2> implements Event {

    public native void send(T1 arg1, T2 arg2);
}
