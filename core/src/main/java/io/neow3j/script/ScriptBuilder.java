package io.neow3j.script;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.types.CallFlags;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.ArrayUtils;
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

import static java.lang.String.format;
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
     * Appends OpCodes to the script in the order provided.
     *
     * @param opCodes the OpCodes to append.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder opCode(OpCode... opCodes) {
        for (OpCode o : opCodes) {
            writeByte(o.getCode());
        }
        return this;
    }

    /**
     * Appends an OpCode and a belonging argument to the script.
     *
     * @param opCode   the OpCode to append.
     * @param argument the argument of the OpCode.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder opCode(OpCode opCode, byte[] argument) {
        writeByte(opCode.getCode());
        write(argument);
        return this;
    }

    /**
     * Appends a call to the contract denoted by the given script hash. Uses {@link CallFlags#ALL} for the call.
     *
     * @param hash160 the script hash of the contract to call.
     * @param method  the method to call.
     * @param params  the parameters that will be used in the call. Need to be in correct order.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder contractCall(Hash160 hash160, String method, List<ContractParameter> params) {
        return contractCall(hash160, method, params, CallFlags.ALL);
    }

    /**
     * Appends a call to the contract denoted by the given script hash.
     *
     * @param hash160   the script hash of the contract to call.
     * @param method    the method to call.
     * @param params    the parameters that will be used in the call. Need to be in correct order.
     * @param callFlags the call flags to use for the contract call.
     * @return this ScriptBuilder object.
     */
    public ScriptBuilder contractCall(Hash160 hash160, String method, List<ContractParameter> params,
            CallFlags callFlags) {

        if (params != null && params.size() > 0) {
            pushParams(params);
        } else {
            opCode(OpCode.NEWARRAY0);
        }
        pushInteger(callFlags.getValue());
        pushData(method);
        pushData(hash160.toLittleEndianArray());
        sysCall(InteropService.SYSTEM_CONTRACT_CALL);
        return this;
    }

    public ScriptBuilder sysCall(InteropService operation) {
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
     * This method should also be used if the parameters list is empty. In that case the script looks like the
     * following:
     * <pre>
     *  PUSH0
     *  PACK
     * </pre>
     *
     * @param params the list of parameters to add.
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
        if (param == null) {
            opCode(OpCode.PUSHNULL);
        } else {
            Object value = param.getValue();
            switch (param.getType()) {
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
                    pushData(((Hash256) value).toLittleEndianArray());
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
                    throw new IllegalArgumentException(format("Parameter type '%s' not supported.", param.getType()));
            }
        }
        return this;
    }

    /**
     * Adds a push operation with the given integer to the script.
     *
     * @param v the number to push.
     * @return this.
     * @throws IllegalArgumentException if the given number is smaller than -1.
     */
    public ScriptBuilder pushInteger(long v) {
        return pushInteger(BigInteger.valueOf(v));
    }

    private static final BigInteger minusOne = BigInteger.valueOf(-1);
    private static final BigInteger sixteen = BigInteger.valueOf(16);

    /**
     * Adds a push operation with the given integer to the script. The integer is encoded in its two's complement and
     * in little-endian order.
     * <p>
     * The integer can be up to 32 bytes long.
     *
     * @param v the integer to push.
     * @return this.
     * @throws IllegalArgumentException if the given integer is smaller than -1 or takes more space than 32 bytes.
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
            return this.opCode(OpCode.PUSHINT32, padNumber(v, 4));
        }
        if (bytes.length <= 8) {
            return this.opCode(OpCode.PUSHINT64, padNumber(v, 8));
        }
        if (bytes.length <= 16) {
            return this.opCode(OpCode.PUSHINT128, padNumber(v, 16));
        }
        if (bytes.length <= 32) {
            return this.opCode(OpCode.PUSHINT256, padNumber(v, 32));
        }
        throw new IllegalArgumentException("The given number (" + v + ") is out of range.");
    }

    private byte[] padNumber(BigInteger v, int desiredLength) {
        if (v.toByteArray().length == desiredLength) {
            return BigIntegers.toLittleEndianByteArray(v);
        }
        if (v.signum() == -1) {
            // If the number is negative we need to pad it with 1's to keep it a negative number.
            byte[] data = v.toByteArray();
            byte[] paddedData = new byte[desiredLength];
            System.arraycopy(data, 0, paddedData, paddedData.length - data.length, data.length);
            for (int i = 0; i < paddedData.length - data.length; i++) {
                paddedData[i] = (byte) 255;
            }
            return ArrayUtils.reverseArray(paddedData);
        } else {
            // If the number is positive we just pad it with zeros.
            byte[] data = BigIntegers.toLittleEndianByteArray(v);
            byte[] paddedData = new byte[desiredLength];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            return paddedData;
        }
    }

    public ScriptBuilder pushBoolean(boolean bool) {
        if (bool) {
            writeByte(OpCode.PUSHT.getCode());
        } else {
            writeByte(OpCode.PUSHF.getCode());
        }
        return this;
    }

    /**
     * Adds the data to the script, prefixed with the correct code for its length.
     *
     * @param data the data to add to the script.
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
     * @param data the data to add to the script.
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
        if (params == null || params.length == 0) {
            opCode(OpCode.NEWARRAY0);
        } else {
            for (int i = params.length - 1; i >= 0; i--) {
                pushParam(params[i]);
            }
            pushInteger(params.length);
            pack();
        }
        return this;
    }

    public ScriptBuilder pushMap(HashMap<ContractParameter, ContractParameter> map) {
        for (Map.Entry<ContractParameter, ContractParameter> entry : map.entrySet()) {
            pushParam(entry.getValue());
            pushParam(entry.getKey());
        }
        pushInteger(map.size());
        opCode(OpCode.PACKMAP);
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
     * @param encodedPublicKey the public key encoded in compressed format.
     * @return the script.
     */
    public static byte[] buildVerificationScript(byte[] encodedPublicKey) {
        return new ScriptBuilder()
                .pushData(encodedPublicKey)
                .sysCall(InteropService.SYSTEM_CRYPTO_CHECKSIG)
                .toArray();
    }

    /**
     * Builds a verification script for a multi signature account from the given public keys.
     *
     * @param pubKeys          the public keys.
     * @param signingThreshold the desired minimum number of signatures required when using the multi-sig account.
     * @return the script.
     */
    public static byte[] buildVerificationScript(List<ECKeyPair.ECPublicKey> pubKeys, int signingThreshold) {
        ScriptBuilder builder = new ScriptBuilder().pushInteger(signingThreshold);
        pubKeys.stream().sorted().forEach(k -> builder.pushData(k.getEncoded(true)));
        return builder
                .pushInteger(pubKeys.size())
                .sysCall(InteropService.SYSTEM_CRYPTO_CHECKMULTISIG)
                .toArray();
    }

    /**
     * Calculates the script of the contract hash deployed by {@code sender}.
     * <p>
     * A contract's hash doesn't change after deployment. Even if the contract's script is updated the hash stays the
     * same. It depends on the initial NEF checksum, contract name, and the sender of the deployment transaction.
     *
     * @param sender       the account that deployed the contract.
     * @param nefCheckSum  the checksum of the contract's NEF file.
     * @param contractName the contract's name.
     * @return the bytes of the contract hash.
     */
    public static byte[] buildContractHashScript(Hash160 sender, long nefCheckSum, String contractName) {
        return new ScriptBuilder()
                .opCode(OpCode.ABORT)
                .pushData(sender.toLittleEndianArray())
                .pushInteger(nefCheckSum)
                .pushData(contractName)
                .toArray();
    }

    /**
     * Builds a script that calls a contract method with the provided parameters and {@link CallFlags#ALL} where the
     * return value is expected to be an iterator. The iterator is then traversed and its values are added to an array.
     * <p>
     * Use this to retrieve iterator values in interaction with an RPC server that has sessions disabled.
     *
     * @param contractHash           the script hash of the contract to call.
     * @param method                 the method to call.
     * @param params                 the parameters that will be used in the call. Need to be in correct order.
     * @param maxIteratorResultItems the maximal number of iterator result items to include in the array. This value
     *                               must not exceed NeoVM limits.
     * @return the script.
     */
    public static byte[] buildContractCallAndUnwrapIterator(Hash160 contractHash, String method,
            List<ContractParameter> params, int maxIteratorResultItems) {

        return buildContractCallAndUnwrapIterator(contractHash, method, params, CallFlags.ALL,
                maxIteratorResultItems);
    }

    /**
     * Builds a script that calls a contract method with the provided parameters where the return value is expected
     * to be an iterator. The iterator is then traversed and its values are added to an array.
     * <p>
     * Use this to retrieve iterator values in interaction with an RPC server that has sessions disabled.
     * <p>
     * Thanks to Anna Shaleva and Roman Khimov for the
     * <a href="https://github.com/nspcc-dev/neo-go/blob/d4292ed5326e11aaa9fa53fe35459acd6a0e3239/pkg/smartcontract/entry.go#L21">implementation in neo-go</a>
     * on which this method is based on.
     *
     * @param contractHash           the script hash of the contract to call.
     * @param method                 the method to call.
     * @param params                 the parameters that will be used in the call. Need to be in correct order.
     * @param callFlags              the call flags to use for the contract call.
     * @param maxIteratorResultItems the maximal number of iterator result items to include in the array. This value
     *                               must not exceed NeoVM limits.
     * @return the script.
     */
    public static byte[] buildContractCallAndUnwrapIterator(Hash160 contractHash, String method,
            List<ContractParameter> params, CallFlags callFlags, int maxIteratorResultItems) {

        ScriptBuilder b = new ScriptBuilder();                   // Stack state after opcode execution (top to bottom).
        b.pushInteger(maxIteratorResultItems);                   // Stack: maxIt
        b.contractCall(contractHash, method, params, callFlags); // Stack: iterator, maxIt
        // Push an empty array to fill with the iterator result items.
        b.opCode(OpCode.NEWARRAY0);                              // Stack: empty array, iterator, maxIt

        // ### Start of iterator traversal cycle ###
        BigInteger iteratorTraverseCycleStartOffset = BigInteger.valueOf(b.stream.size());
        // Copy the iterator to the top of the stack.
        b.opCode(OpCode.OVER);                                   // Stack: iterator, array, iterator, maxIt
        // Pops the iterator and pushes true or false (whether the iterator has a next value).
        b.sysCall(InteropService.SYSTEM_ITERATOR_NEXT);          // Stack: boolean, array, iterator, maxIt

        // Jump to end of program if iterator has no next value. Operand is set once the offset at cycle end is known.
        BigInteger jmpIfNotOffset = BigInteger.valueOf(b.stream.size());
        b.opCode(OpCode.JMPIFNOT, new byte[]{0x00});             // Stack: array, iterator, maxIt

        // Duplicate the array and
        b.opCode(OpCode.DUP,                                     // Stack: array, array, iterator, maxIt
                OpCode.PUSH2, OpCode.PICK);                      // Stack: iterator, array, array, iterator, maxIt
        b.sysCall(InteropService.SYSTEM_ITERATOR_VALUE);         // Stack: iteratorItem, array, array, iterator, maxIt
        b.opCode(OpCode.APPEND,                                  // Stack: array, iterator, maxIt
                OpCode.DUP,                                      // Stack: array, array, iterator, maxIt
                OpCode.SIZE,                                     // Stack: arraySize, array, iterator, maxIt
                OpCode.PUSH3, OpCode.PICK,                       // Stack: maxIt, arraySize, array, iterator, maxIt
                OpCode.GE);                                      // Stack: arraysize >= maxIt, array, iterator, maxIt

        // Jump to end of cycle if array holds the defined maximal number of iterator result items.
        // Operand is set once the offset at cycle end is known.
        BigInteger jmpIfMaxReachedOffset = BigInteger.valueOf(b.stream.size());
        b.opCode(OpCode.JMPIF, new byte[]{0x00});                // Stack: array, iterator, maxIt

        // Jump to start of iterator traversal cycle.
        BigInteger jmpOffset = BigInteger.valueOf(b.stream.size());
        byte jmpBytesToCycleStart = iteratorTraverseCycleStartOffset.subtract(jmpOffset).byteValue();
        b.opCode(OpCode.JMP, new byte[]{jmpBytesToCycleStart});  // Stack: array, iterator, maxIt

        // ### End of traversal cycle ###
        // Remove the iterator and maxIteratorResultItems.
        BigInteger loadResultOffset = BigInteger.valueOf(b.stream.size());
        b.opCode(OpCode.NIP,                                     // Stack: array, maxIt
                OpCode.NIP);                                     // Stack: array

        // Fill in operands for the jump opcodes.
        byte[] bytes = b.toArray();
        bytes[jmpIfNotOffset.add(BigInteger.ONE).byteValue()] = loadResultOffset.subtract(jmpIfNotOffset).byteValue();
        bytes[jmpIfMaxReachedOffset.add(BigInteger.ONE).byteValue()] =
                loadResultOffset.subtract(jmpIfMaxReachedOffset).byteValue();
        return bytes;
    }

}
