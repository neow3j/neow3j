package io.neow3j.contract;

import io.neow3j.constants.OpCode;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.BigIntegers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ScriptBuilder {

    private DataOutputStream stream;
    private ByteBuffer buffer;
    private ByteArrayOutputStream byteStream;

    public ScriptBuilder() {
        byteStream = new ByteArrayOutputStream();
        stream = new DataOutputStream(byteStream);
        buffer = ByteBuffer.wrap(new byte[8]).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Appends an OpCode to the script.
     *
     * @param opCode The OpCode to append.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder opCode(OpCode opCode) {
        writeByte(opCode.getValue());
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call.
     * @param operation  The operation to call.
     * @param params     The parameters that will be used in the app call. Need to be in correct order.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder appCall(ScriptHash scriptHash, String operation,
                                 List<ContractParameter> params) {

        if (params == null || params.isEmpty()) {
            if (operation == null) {
                return appCall(scriptHash);
            } else {
                return appCall(scriptHash, operation);
            }
        } else if (operation == null) {
            return appCall(scriptHash, params);
        }

        for (int i = params.size() - 1; i >= 0; i--) {
            pushParam(params.get(i));
        }
        pushInteger(params.size());
        opCode(OpCode.PACK);
        pushData(operation);
        appCall(scriptHash);
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call.
     * @param params     The parameters that will be used in the app call. Need to be in correct order.
     * @return this ScriptBuilder object.
     */
    private ScriptBuilder appCall(ScriptHash scriptHash, List<ContractParameter> params) {
        for (int i = params.size() - 1; i >= 0; i--) {
            pushParam(params.get(i));
        }
        appCall(scriptHash);
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call.
     * @param operation  The operation to call.
     * @return this ScriptBuilder object.
     */
    private ScriptBuilder appCall(ScriptHash scriptHash, String operation) {
        pushBoolean(false);
        pushData(operation);
        appCall(scriptHash);
        return this;
    }

    private ScriptBuilder appCall(ScriptHash scriptHash) {
        return call(scriptHash, OpCode.APPCALL);
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call in big-endian order.
     * @param operation  The operation to call.
     * @param params     The parameters that will be used in the app call. Need to be in correct order.
     * @return this ScriptBuilder object.
     * @deprecated User {@link ScriptBuilder#appCall(ScriptHash, String, List)} instead.
     */
    @Deprecated
    public ScriptBuilder appCall(byte[] scriptHash, String operation,
                                 List<ContractParameter> params) {

        appCall(new ScriptHash(ArrayUtils.reverseArray(scriptHash)), operation, params);
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call in big-endian order.
     * @param params     The parameters that will be used in the app call. Need to be in correct order.
     * @return this ScriptBuilder object.
     * @deprecated User {@link ScriptBuilder#appCall(ScriptHash, List)} instead.
     */
    @Deprecated
    public ScriptBuilder appCall(byte[] scriptHash, List<ContractParameter> params) {
        appCall(new ScriptHash(ArrayUtils.reverseArray(scriptHash)), params);
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call in big-endian order.
     * @param operation  The operation to call.
     * @return this ScriptBuilder object.
     * @deprecated User {@link ScriptBuilder#appCall(ScriptHash, String)} instead.
     */
    @Deprecated
    public ScriptBuilder appCall(byte[] scriptHash, String operation) {
        appCall(new ScriptHash(ArrayUtils.reverseArray(scriptHash)), operation);
        return this;
    }

    /**
     * Appends an app call to the script.
     *
     * @param scriptHash The script hash of the contract to call in big-endian order.
     * @return this ScriptBuilder object.
     * @deprecated User {@link ScriptBuilder#appCall(ScriptHash)} instead.
     */
    @Deprecated
    public ScriptBuilder appCall(byte[] scriptHash) {
        return call(new ScriptHash(ArrayUtils.reverseArray(scriptHash)), OpCode.APPCALL);
    }

    /**
     * Appends a tail call to the script.
     *
     * @param scriptHash The script hash of the contract to call in big-endian order.
     * @return this ScriptBuilder object.
     * @deprecated User {@link ScriptBuilder#tailCall(ScriptHash)} instead.
     */
    @Deprecated
    public ScriptBuilder tailCall(byte[] scriptHash) {
        return tailCall(new ScriptHash(ArrayUtils.reverseArray(scriptHash)));
    }

    /**
     * Appends a tail call to the script.
     *
     * @param scriptHash The script hash of the contract to call.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder tailCall(ScriptHash scriptHash) {
        return call(scriptHash, OpCode.TAILCALL);
    }

    private ScriptBuilder call(ScriptHash scriptHash, OpCode opCode) {
        writeByte(opCode.getValue());
        write(scriptHash.toArray());
        return this;
    }

    public ScriptBuilder sysCall(String operation) {
        if (operation.length() == 0)
            throw new IllegalArgumentException("Provided operation string is empty.");

        byte[] operationBytes = operation.getBytes(UTF_8);
        if (operationBytes.length > 252)
            throw new IllegalArgumentException("Provided operation is too long.");

        byte[] callArgument = ArrayUtils.concatenate((byte) operationBytes.length, operationBytes);
        writeByte(OpCode.SYSCALL.getValue());
        write(callArgument);
        return this;
    }

    public ScriptBuilder pushParam(ContractParameter param) {
        Object value = param.getValue();
        switch (param.getParamType()) {
            case BYTE_ARRAY:
            case SIGNATURE:
                pushData((byte[]) value);
                break;
            case BOOLEAN:
                pushBoolean((boolean) value);
                break;
            case INTEGER:
                pushInteger((BigInteger) value);
                break;
            case HASH160:
            case HASH256:
                pushData(((ScriptHash) value).toArray());
                break;
            case STRING:
                pushData((String) value);
                break;
            case ARRAY:
                pushArray((ContractParameter[]) value);
                break;
            case PUBLIC_KEY:
                // TODO 10.07.19 claude: Implement public key push operation.
            default:
                throw new IllegalArgumentException("Parameter type \'" + param.getParamType() +
                        "\' not supported.");
        }
        return this;
    }

    public ScriptBuilder pushInteger(int v) {
        return pushInteger(BigInteger.valueOf(v));
    }

    public ScriptBuilder pushInteger(BigInteger number) {
        if (number.intValue() == -1) {
            writeByte(OpCode.PUSHM1.getValue());
        } else if (number.intValue() == 0) {
            writeByte(OpCode.PUSH0.getValue());
        } else if (number.intValue() >= 1 && number.intValue() <= 16) {
            // OpCodes PUSH1 to PUSH16
            int base = (OpCode.PUSH1.getValue() - 1);
            writeByte(base + number.intValue());
        } else {
            // If the number is larger than 16, it needs to be pushed as a data array.
            pushData(BigIntegers.toLittleEndianByteArray(number));
        }
        return this;
    }

    public ScriptBuilder pushBoolean(boolean bool) {
        if (bool) {
            writeByte(OpCode.PUSHT.getValue());
        } else {
            writeByte(OpCode.PUSHF.getValue());
        }
        return this;
    }

    /**
     * Adds the data to the script, prefixed with the correct code for its length.
     *
     * @param data The data to add to the script.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder pushData(String data) {
        if (data != null) {
            pushData(data.getBytes(UTF_8));
        } else {
            pushData("".getBytes());
        }
        return this;
    }

    /**
     * Adds the data to the script, prefixed with the correct code for its length.
     *
     * @param data The data to add to the script.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder pushData(byte[] data) {
        pushDataLength(data.length);
        write(data);
        return this;
    }

    public ScriptBuilder pushDataLength(int length) {
        if (length <= OpCode.PUSHBYTES75.getValue()) {
            // For up to 75 bytes of data we can use the OpCodes PUSHBYTES01 to PUSHBYTES75 directly.
            writeByte(length);
        } else if (length <= 255) {
            // If the data is 76 to 255 (0xff) bytes long then write PUSHDATA1 + uint8
            writeByte(OpCode.PUSHDATA1.getValue());
            writeByte(length);
        } else if (length <= 65535) {
            // If the data is 256 to 65535 (0xffff) bytes long then write PUSHDATA2 + uint16
            writeByte(OpCode.PUSHDATA2.getValue());
            writeShort(length);
        } else {
            // If the data is bigger than 65536 then write PUSHDATA4 + uint32
            writeByte(OpCode.PUSHDATA4.getValue());
            writeInt(length);
        }
        return this;
    }

    public ScriptBuilder pushArray(ContractParameter[] params) {
        for (int i = params.length - 1; i >= 0; i--) {
            pushParam(params[i]);
        }
        pushInteger(params.length);
        opCode(OpCode.PACK);
        return this;
    }

    private void writeByte(int v) {
        try {
            stream.writeByte(v);
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
    }

    private void writeShort(int v) {
        buffer.putInt(0, v);
        try {
            stream.write(buffer.array(), 0, 2);
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
    }

    private void writeInt(int v) {
        buffer.putInt(0, v);
        try {
            stream.write(buffer.array(), 0, 4);
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
    }

    private void write(byte[] data) {
        try {
            stream.write(data);
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
    }

    private void writeReversed(byte[] data) {
        try {
            stream.write(ArrayUtils.reverseArray(data));
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
    }

    public byte[] toArray() {
        try {
            stream.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
        return byteStream.toByteArray();
    }
}
