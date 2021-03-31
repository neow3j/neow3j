package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.model.types.CallFlags;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unchecked")
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
        writeByte(opCode.getCode());
        return this;
    }

    /**
     * Appends an OpCode and a belonging argument to the script.
     *
     * @param opCode   The OpCode to append.
     * @param argument The argument of the OpCode.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder opCode(OpCode opCode, byte[] argument) {
        writeByte(opCode.getCode());
        write(argument);
        return this;
    }

    /**
     * Appends a call to the contract denoted by the given script hash. Uses {@link CallFlags#ALL}
     * for the call.
     *
     * @param hash160 The script hash of the contract to call.
     * @param method  The method to call.
     * @param params  The parameters that will be used in the call. Need to be in correct order.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder contractCall(Hash160 hash160, String method,
            List<ContractParameter> params) {

        return contractCall(hash160, method, params, CallFlags.ALL);
    }

    /**
     * Appends a call to the contract denoted by the given script hash.
     *
     * @param hash160   The script hash of the contract to call.
     * @param method    The method to call.
     * @param params    The parameters that will be used in the call. Need to be in correct order.
     * @param callFlags The call flags to use for the contract call.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder contractCall(Hash160 hash160, String method,
            List<ContractParameter> params, CallFlags callFlags) {

        if (params.size() > 0) {
            pushParams(params);
        } else {
            opCode(OpCode.NEWARRAY0);
        }
        pushInteger(callFlags.getValue());
        pushData(method);
        pushData(hash160.toLittleEndianArray());
        sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALL);
        return this;
    }

    public ScriptBuilder sysCall(InteropServiceCode operation) {
        writeByte(OpCode.SYSCALL.getCode());
        write(Numeric.hexStringToByteArray(operation.getHash()));
        return this;
    }

    /**
     * Adds the given contract parameters to the script.
     * <p>
     * Example with two parameters in the list:
     * <pre>
     *  PUSHBYTES4 a3b00183
     *  PUSHBYTES20 0100000000000000000000000000000000000000
     *  PUSH2
     *  PACK
     * </pre>
     * <p>
     * This method should also be used if the parameters list is empty. In that case the script
     * looks like the following:
     * <pre>
     *  PUSH0
     *  PACK
     * </pre>
     *
     * @param params The list of parameters to add.
     * @return this
     */
    public ScriptBuilder pushParams(List<ContractParameter> params) {
        // Push params in reverse order.
        for (int i = params.size() - 1; i >= 0; i--) {
            pushParam(params.get(i));
        }
        // Even if the parameter list is empty we need to push PUSH0 and PACK OpCodes.
        pushInteger(params.size());
        opCode(OpCode.PACK);
        return this;
    }

    public ScriptBuilder pushParam(ContractParameter param) {
        Object value = param.getValue();
        switch (param.getParamType()) {
            case BYTE_ARRAY:
            case SIGNATURE:
            case PUBLIC_KEY:
                pushData((byte[]) value);
                break;
            case BOOLEAN:
                pushBoolean((boolean) value);
                break;
            case INTEGER:
                pushInteger((BigInteger) value);
                break;
            case HASH160:
                pushData(((Hash160) value).toLittleEndianArray());
                break;
            case HASH256:
                pushData(((Hash256) value).toArray());
                break;
            case STRING:
                pushData((String) value);
                break;
            case ARRAY:
                pushArray((ContractParameter[]) value);
                break;
            case MAP:
                pushMap((HashMap<ContractParameter, ContractParameter>) value);
                break;
            case ANY:
                if (value == null) {
                    opCode(OpCode.PUSHNULL);
                }
                break;
            default:
                throw new IllegalArgumentException("Parameter type '" + param.getParamType() +
                        "' not supported.");
        }
        return this;
    }

    /**
     * Adds a push operation with the given integer to the script.
     *
     * @param v The number to push.
     * @return this.
     * @throws IllegalArgumentException if the given number is smaller than -1.
     */
    public ScriptBuilder pushInteger(long v) {
        return pushInteger(BigInteger.valueOf(v));
    }

    private static final BigInteger minusOne = BigInteger.valueOf(-1);
    private static final BigInteger sixteen = BigInteger.valueOf(16);

    /**
     * Adds a push operation with the given integer to the script. The integer is encoded in its
     * two's complement and in little-endian order.
     * <p>
     * The integer can be up to 32 bytes long.
     *
     * @param v The integer to push.
     * @return this.
     * @throws IllegalArgumentException if the given integer is smaller than -1 or takes more space
     *                                  than 32 bytes.
     */
    public ScriptBuilder pushInteger(BigInteger v) {
        int i = v.intValue();
        if (v.compareTo(minusOne) >= 0 && v.compareTo(sixteen) <= 0) {
            int opcode = OpCode.PUSH0.getCode() + i;
            return this.opCode(OpCode.get(opcode));
        }

        byte[] bytes = BigIntegers.toLittleEndianByteArray(v);
        if (bytes.length == 1) {
            return this.opCode(OpCode.PUSHINT8, bytes);
        }
        if (bytes.length == 2) {
            return this.opCode(OpCode.PUSHINT16, bytes);
        }
        if (bytes.length <= 4) {
            return this.opCode(OpCode.PUSHINT32, padRight(bytes, 4));
        }
        if (bytes.length <= 8) {
            return this.opCode(OpCode.PUSHINT64, padRight(bytes, 8));
        }
        if (bytes.length <= 16) {
            return this.opCode(OpCode.PUSHINT128, padRight(bytes, 16));
        }
        if (bytes.length <= 32) {
            return this.opCode(OpCode.PUSHINT256, padRight(bytes, 32));
        }
        throw new IllegalArgumentException("The given number (" + v.toString() + ") is out of "
                + "range.");
    }

    private byte[] padRight(byte[] data, int desiredLenght) {
        if (data.length >= desiredLenght) {
            return data;
        }
        byte[] paddedData = new byte[desiredLenght];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        return paddedData;
    }

    public ScriptBuilder pushBoolean(boolean bool) {
        if (bool) {
            writeByte(OpCode.PUSH1.getCode());
        } else {
            writeByte(OpCode.PUSH0.getCode());
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
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }
        if (data.length < 256) {
            this.opCode(OpCode.PUSHDATA1);
            this.writeByte((byte) data.length);
            this.write(data);
        } else if (data.length < 65536) {
            this.opCode(OpCode.PUSHDATA2);
            this.writeShort(data.length);
            this.write(data);
        } else {
            this.opCode(OpCode.PUSHDATA4);
            this.writeInt(data.length);
            this.write(data);
        }
        return this;
    }

    public ScriptBuilder pushArray(ContractParameter[] params) {
        for (int i = params.length - 1; i >= 0; i--) {
            pushParam(params[i]);
        }
        pushInteger(params.length);
        pack();
        return this;
    }

    public ScriptBuilder pushMap(HashMap<ContractParameter, ContractParameter> map) {
        opCode(OpCode.NEWMAP);
        if (map != null) {
            for (Map.Entry<ContractParameter, ContractParameter> entry : map.entrySet()) {
                opCode(OpCode.DUP);
                pushParam(entry.getKey());
                pushParam(entry.getValue());
                opCode(OpCode.SETITEM);
            }
        }
        return this;
    }


    public ScriptBuilder pack() {
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

    public byte[] toArray() {
        try {
            stream.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException without doing IO.");
        }
        return byteStream.toByteArray();
    }

    /**
     * Builds a verification script for the given public key.
     *
     * @param encodedPublicKey The public key encoded in compressed format.
     * @return the script.
     */
    public static byte[] buildVerificationScript(byte[] encodedPublicKey) {
        return new ScriptBuilder()
                .pushData(encodedPublicKey)
                .sysCall(InteropServiceCode.NEO_CRYPTO_CHECKSIG)
                .toArray();
    }

    /**
     * Builds a verification script for a multi signature account from the given public keys.
     *
     * @param encodedPublicKeys The public keys encoded in compressed format.
     * @param signingThreshold  The desired minimum number of signatures required when using the
     *                          multi-sig account.
     * @return the script.
     */
    public static byte[] buildVerificationScript(List<byte[]> encodedPublicKeys,
            int signingThreshold) {
        ScriptBuilder builder = new ScriptBuilder().pushInteger(signingThreshold);
        encodedPublicKeys.forEach(builder::pushData);
        return builder
                .pushInteger(encodedPublicKeys.size())
                .sysCall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIG)
                .toArray();
    }

}
