package io.neow3j.devpack.neo;

import io.neow3j.devpack.annotations.Contract;

@Contract(scriptHash = "0xb1c37d5847c2ae36bdde31d0cc833a7ad9667f8f")
public class Oracle {

    /**
     * Gets the script hash of the Oracle contract.
     *
     * @return the script hash.
     */
    public static native byte[] hash();


    /**
     * Does a request to the oracle service with the given request data. The given callback function
     * will be called with the response of the oracle as input.
     *
     * @param url            The URL to query.
     * @param filter         The filter to filter return data.
     * @param callback       The callback function.
     * @param userData       Additional data.
     * @param gasForResponse The cost of getting a response.
     */
    // TODO: Adapt documentation as soon as Neo core developers have created documentation.
    public static native void request(String url, String filter, String callback, Object userData,
            int gasForResponse);
}
