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
 */
public @interface CallFlags {

    byte value();

}
