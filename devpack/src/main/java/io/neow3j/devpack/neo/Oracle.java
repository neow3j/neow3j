package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

@Contract(scriptHash = "0x3c05b488bf4cf699d0631bf80190896ebbf38c3b")
public class Oracle {

    /**
     * Gets the name of the Oracle contract.
     *
     * @return the name.
     */
    public static native String name();

    /**
     * Gets the script hash of the Oracle contract.
     *
     * @return the script hash.
     */
    public static native byte[] hash();

    /**
     * // TODO: Adapt documentation as soon as Neo core developers have created documentation. Does
     * a request to the oracle service with the given request data. The given callback function will
     * be called with the response of the oracle as input.
     *
     * @param url            The URL to query.
     * @param filter         The filter to filter return data.
     * @param callback       The callback function.
     * @param userData       Additional data.
     * @param gasForResponse The cost of getting a response.
     */
    public static native void request(String url, String filter, String callback,
            Object userData, long gasForResponse);
}
