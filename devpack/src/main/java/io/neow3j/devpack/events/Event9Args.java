package io.neow3j.devpack.events;

/**
 * Use this class to create an event that takes nine arguments.
 * <p>
 * It is recommended to annotate this event with {@link io.neow3j.devpack.annotations.EventParameterNames} to provide
 * more information about the event in the contract's ABI.
 */
public class Event9Args<T1, T2, T3, T4, T5, T6, T7, T8, T9> implements EventInterface {

    public native void fire(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9);

}
