package io.neow3j.transaction;

import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.utils.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;

/**
 * A high priority attribute can be used by committee members to prioritize a transaction.
 */
public class OracleResponseAttribute extends TransactionAttribute {

    /**
     * The maximum size of the {@code Result} field.
     */
    public final static int MAX_RESULT_SIZE = 0xffff; // 65'535

    /**
     * The ID of the oracle request.
     * <p>
     * The ID is represented as an unsigned integer by the Neo node, i.e., its range is [0, 2^64].
     */
    public BigInteger id;

    /**
     * The response code.
     */
    public OracleResponseCode code;

    /**
     * The response data.
     */
    public byte[] result;

    public OracleResponseAttribute() {
        super(TransactionAttributeType.ORACLE_RESPONSE);
    }

    /**
     * @return the ID of the oracle request/response.
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * @return the response code of oracle response.
     */
    public OracleResponseCode getCode() {
        return code;
    }

    /**
     * @return the result bytes of the oracle response.
     */
    public byte[] getResult() {
        return result;
    }

    @Override
    protected int getSizeWithoutType() {
        return 8 // Id: 64 bit, 8 byte
                + 1 // ResponseCode
                + IOUtils.getVarSize(result); //Result
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws IOException {
        // The ID can be bigger than java long.
        id = BigIntegers.fromLittleEndianByteArray(reader.readBytes(8));
        code = OracleResponseCode.valueOf(reader.readByte());
        result = reader.readVarBytes(MAX_RESULT_SIZE);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.write(BigIntegers.toLittleEndianByteArray(id));
        writer.writeByte(code.byteValue());
        writer.writeVarBytes(result);
    }

}
