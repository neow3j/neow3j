package io.neow3j.devpack;

/**
 * Contains the possible respond codes that are used by Oracles.
 */
public class OracleResponseCode {

    public static final byte SUCCESS =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.SUCCESS.byteValue();

    public static final byte PROTOCOL_NOT_SUPPORTED =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.PROTOCOL_NOT_SUPPORTED.byteValue();

    public static final byte CONSENSUS_UNREACHABLE =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.CONSENSUS_UNREACHABLE.byteValue();

    public static final byte NOT_FOUND =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.NOT_FOUND.byteValue();

    public static final byte TIMEOUT =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.TIMEOUT.byteValue();

    public static final byte FORBIDDEN =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.FORBIDDEN.byteValue();

    public static final byte RESPONSE_TOO_LARGE =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.RESPONSE_TOO_LARGE.byteValue();

    public static final byte INSUFFICIENT_FUNDS =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.INSUFFICIENT_FUNDS.byteValue();

    public static final byte ERROR =
            io.neow3j.protocol.core.methods.response.OracleResponseCode.ERROR.byteValue();

}
