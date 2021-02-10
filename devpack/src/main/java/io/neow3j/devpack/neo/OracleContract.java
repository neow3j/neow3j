package io.neow3j.devpack.neo;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0x8dc0e742cbdfdeda51ff8a8b78d46829144c80ee")
public class OracleContract extends ContractInterface {

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
