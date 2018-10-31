package com.axlabs.neow3j.protocol.core.polling;

/**
 * Filter callback interface.
 */
public interface Callback<T> {
    void onEvent(T value);
}
