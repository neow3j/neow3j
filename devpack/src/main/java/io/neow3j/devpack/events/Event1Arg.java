package io.neow3j.devpack.events;

/**
 * Use this class to create an event that takes 1 argument.
 * <p>
 * It is recommended to annotate this event with {@link io.neow3j.devpack.annotations.EventParameterNames} to provide
 * more information about the event in the contract's ABI.
 */
public class Event1Arg<T1> implements EventInterface {

    public native void fire(T1 arg1);

}
