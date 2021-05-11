package io.neow3j.devpack;

/**
 * Contains the possible respond codes that are used by Oracles.
 */
public class OracleResponseCode {

    public static final byte SUCCESS = 0;

    public static final byte PROTOCOL_NOT_SUPPORTED = 0x10;

    public static final byte CONSENSUS_UNREACHABLE = 0x12;

    public static final byte NOT_FOUND = 0x14;

    public static final byte TIMEOUT = 0x16;

    public static final byte FORBIDDEN = 0x18;

    public static final byte RESPONSE_TOO_LARGE = 0x1a;

    public static final byte INSUFFICIENT_FUNDS = 0x1c;

    public static final byte ERROR = (byte) 0xff;

}
