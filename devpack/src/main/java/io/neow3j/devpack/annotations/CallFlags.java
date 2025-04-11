package io.neow3j.devpack.annotations;

/**
 * This annotation is used on smart contract interface methods to specify what operations are allowed when they are
 * called. When this annotation is absent, {@link io.neow3j.devpack.constants.CallFlags#All} is used by default.
 * <p>
 * Use the constants in {@link io.neow3j.devpack.constants.CallFlags} to set the desired value.
 * <p>
 * For example:
 * <p>{@code @CallFlags(ReadStates)}
 * <p>{@code public native ByteString getStorageValue();}</p>
 * <p>
 * The contract interfaces in neow3j use looser call flags than potentially required ones. Functions that generally
 * should not need to write any state are marked with {@link io.neow3j.devpack.constants.CallFlags#ReadOnly} that
 * allows to read states and call other contracts (i.e., for potential wrapping cases where a read call would be
 * relayed). Any other functions are marked with the call flag {@link io.neow3j.devpack.constants.CallFlags#All} to
 * more transparently show that they use all call flags.
 * <p>
 * If you need to use more restrictive call flags with one of neow3j's provided contract interfaces, you can
 * extend the interface and override the method with the desired call flags.
 */
public @interface CallFlags {

    byte value();

}
