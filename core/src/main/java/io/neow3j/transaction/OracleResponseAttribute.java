package io.neow3j.transaction;

import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.utils.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * A high priority attribute can be used by committee members to prioritize a transaction.
 */
public class OracleResponseAttribute extends TransactionAttribute {

    /**
     * The maximum size of the {@code Result} field.
     */
    private final static int MAX_RESULT_SIZE = 0xffff; // 65'535

    /**
     * The ID of the oracle request.
     * <p>
     * The ID is represented as an unsigned integer by the Neo node, i.e., its range is [0, 2^64].
     */
    private BigInteger id;

    /**
     * The response code.
     */
    private OracleResponseCode code;

    /**
     * The response data.
     */
    private byte[] result;

    public OracleResponseAttribute() {
        super(TransactionAttributeType.ORACLE_RESPONSE);
    }

    public OracleResponseAttribute(BigInteger id, OracleResponseCode code, byte[] result) {
        this();
        this.id = id;
        this.code = code;
        this.result = result;
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
        writer.write(BigIntegers.toLittleEndianByteArrayZeroPadded(id, 8));
        writer.writeByte(code.byteValue());
        writer.writeVarBytes(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OracleResponseAttribute)) {
            return false;
        }
        OracleResponseAttribute that = (OracleResponseAttribute) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCode(), that.getCode()) &&
                Arrays.equals(getResult(), that.getResult());
    }

}
