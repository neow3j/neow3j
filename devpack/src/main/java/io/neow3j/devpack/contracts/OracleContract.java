package io.neow3j.devpack.contracts;

import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xfe924b7cfe89ddd271abaf7210a80a7e11178758")
public class OracleContract extends ContractInterface {

    /**
     * The minimum GAS fee necessary on an oracle request to pay for the response.
     */
    public static final int MINIMUM_RESPONSE_FEE = 10000000;

    /**
     * Does a request to the oracle service with the given request data. The given callback function
     * will be called with the response of the oracle as input.
     *
     * @param url            The URL to query.
     * @param filter         The filter to filter returned data with.
     * @param callback       The callback function.
     * @param userData       Additional data.
     * @param gasForResponse The GAS amount to pay for the oracle response.
     */
    public static native void request(String url, String filter, String callback, Object userData,
            int gasForResponse);
}
