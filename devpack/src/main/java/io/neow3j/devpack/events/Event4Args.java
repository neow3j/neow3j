package io.neow3j.devpack.events;

/**
 * Use this class to create an event that takes 4 arguments.
 * <p>
 * It is recommended to annotate this event with {@link io.neow3j.devpack.annotations.EventParameterNames} to provide
 * more information about the event in the contract's ABI.
 */
public class Event4Args<T1, T2, T3, T4> implements EventInterface {

    public native void fire(T1 arg1, T2 arg2, T3 arg3, T4 arg4);

}
