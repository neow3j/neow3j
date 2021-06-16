package io.neow3j.devpack.constants;

/**
 * Contains the possible respond codes that are used by Oracles.
 */
public class OracleResponseCode {

    public static final byte Success = 0;

    public static final byte ProtocolNotSupported = 0x10;

    public static final byte ConsensusUnreachable = 0x12;

    public static final byte NotFound = 0x14;

    public static final byte Timeout = 0x16;

    public static final byte Forbidden = 0x18;

    public static final byte ResponseTooLarge = 0x1a;

    public static final byte InsufficientFunds = 0x1c;

    public static final byte Error = (byte) 0xff;

}
