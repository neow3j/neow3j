package io.neow3j.devpack.constants;

/**
 * Contains the possible respond codes that are used by Oracles.
 */
public class OracleResponseCode {

    /**
     * Indicates that the request has been successfully completed.
     */
    public static final byte Success = 0;

    /**
     * Indicates that the protocol of the request is not supported.
     */
    public static final byte ProtocolNotSupported = 0x10;

    /**
     * Indicates that the oracle nodes cannot reach a consensus on the result of the request.
     */
    public static final byte ConsensusUnreachable = 0x12;

    /**
     * Indicates that the requested Uri does not exist.
     */
    public static final byte NotFound = 0x14;

    /**
     * Indicates that the request was not completed within the specified time.
     */
    public static final byte Timeout = 0x16;

    /**
     * Indicates that there is no permission to request the resource.
     */
    public static final byte Forbidden = 0x18;

    /**
     * Indicates that the data for the response is too large.
     */
    public static final byte ResponseTooLarge = 0x1a;

    /**
     * Indicates that the request failed due to insufficient balance.
     */
    public static final byte InsufficientFunds = 0x1c;

    /**
     * Indicates that the content-type of the request is not supported.
     */
    public static final byte ContentTypeNotSupported = 0x1f;

    /**
     * Indicates that the request failed due to other errors.
     */
    public static final byte Error = (byte) 0xff;

}
