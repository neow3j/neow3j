package io.neow3j.devpack.events;

public class Event1Arg<T1> implements Event {

    public native void notify(T1 arg1);
}
