package io.neow3j.contract;

import static io.neow3j.utils.ArrayUtils.trimTrailingBytes;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.CallFlagsType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌───────────────────────────────────────────────────────────────────────┐
 * │                    NEO Executable Format 3 (NEF3)                     │
 * ├──────────┬───────────────┬────────────────────────────────────────────┤
 * │  Field   │     Type      │                  Comment                   │
 * ├──────────┼───────────────┼────────────────────────────────────────────┤
 * │ Magic    │ uint32        │ Magic header                               │
 * │ Compiler │ byte[64]      │ Compiler name and version                  │
 * ├──────────┼───────────────┼────────────────────────────────────────────┤
 * │ Reserve  │ byte[2]       │ Reserved for future extensions. Must be 0. │
 * │ Tokens   │ MethodToken[] │ Method tokens.                             │
 * │ Reserve  │ byte[2]       │ Reserved for future extensions. Must be 0. │
 * │ Script   │ byte[]        │ Var bytes for the payload                  │
 * ├──────────┼───────────────┼────────────────────────────────────────────┤
 * │ Checksum │ uint32        │ First four bytes of double SHA256 hash     │
 * └──────────┴───────────────┴────────────────────────────────────────────┘
 */
public class NefFile extends NeoSerializable {
    private static final int MAGIC = 0x3346454E; // "NEF3".getBytes(UTF_8));
    private static final int MAGIC_SIZE = 4;
    private static final int COMPILER_SIZE = 64;
    private static final int MAX_SCRIPT_LENGTH = 512 * 1024;
    private static final int CHECKSUM_SIZE = 4;
    private static final int RESERVED_BYTES_SIZE = 2;

    private static final int HEADER_SIZE = MAGIC_SIZE + COMPILER_SIZE;

    private String compiler;
    private List<MethodToken> methodTokens;
    private byte[] checkSum; // 4 bytes unsigned integer.
    private byte[] script;

    public NefFile() {
        methodTokens = new ArrayList<>();
        checkSum = new byte[]{};
        script = new byte[]{};
    }

    /**
     * Constructs a new {@code NefFile} from the given contract information.
     *
     * @param compiler     The compiler name and version with which the contract has been compiled.
     * @param script       The contract's script.
     * @param methodTokens The method tokens of the contract.
     */
    public NefFile(String compiler, byte[] script, List<MethodToken> methodTokens) {
        int compilerSize = compiler.getBytes(UTF_8).length;
        if (compilerSize > COMPILER_SIZE) {
            throw new IllegalArgumentException(format("The compiler name and version string can "
                    + "be max %d bytes long, but was %d bytes long.", COMPILER_SIZE, compilerSize));
        }
        this.compiler = compiler;
        this.script = script;
        this.methodTokens = methodTokens == null ? new ArrayList<>() : methodTokens;

        // Need to initialize the check sum because it is required for calculating the check sum.
        checkSum = new byte[CHECKSUM_SIZE];
        checkSum = computeChecksum(this);
    }

    /**
     * Gets the magic number of NEF files.
     *
     * @return the NEF file magic number.
     */
    public int getMagic() {
        return MAGIC;
    }

    /**
     * Gets the compiler (and version) with which this NEF file has been generated.
     *
     * @return the compiler name and version.
     */
    public String getCompiler() {
        return compiler;
    }

    /**
     * Gets the contract's method tokens.
     * <p>
     * The tokens represent calls to other contracts.
     *
     * @return the contract's method tokens.
     */
    public List<MethodToken> getMethodTokens() {
        return methodTokens;
    }

    /**
     * Gets the contract script.
     *
     * @return the contract script.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * Gets this NEF file's check sum.
     *
     * @return the check sum.
     */
    public byte[] getCheckSum() {
        return checkSum;
    }

    /**
     * Gets the NEF file's check sum as an integer.
     * <p>
     * The check sum bytes of the NEF file are read as a little endian unsigned integer.
     * @return the check sum.
     */
    public long getCheckSumAsInteger() {
        return Numeric.toBigInt(ArrayUtils.reverseArray(checkSum)).longValue();
    }

    /**
     * Gets the byte size of this NEF file when serialized.
     *
     * @return the byte size.
     */
    @Override
    public int getSize() {
        return HEADER_SIZE
                + RESERVED_BYTES_SIZE
                + IOUtils.getVarSize(methodTokens)
                + RESERVED_BYTES_SIZE
                + IOUtils.getVarSize(script)
                + CHECKSUM_SIZE;
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeUInt32(MAGIC);
        writer.writeFixedString(compiler, COMPILER_SIZE);
        writer.writeUInt16(0); // reserved bytes
        writer.writeSerializableVariable(methodTokens);
        writer.writeUInt16(0); // reserved bytes
        writer.writeVarBytes(script);
        writer.write(checkSum);
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            // Magic
            if (reader.readUInt32() != MAGIC) {
                throw new DeserializationException("Wrong magic number in NEF file.");
            }
            // Compiler
            byte[] compilerBytes = reader.readBytes(COMPILER_SIZE);
            compiler = new String(trimTrailingBytes(compilerBytes, (byte) 0), UTF_8);
            // Reserved bytes
            if (reader.readUInt16() != 0) {
                throw new DeserializationException("Reserve bytes in NEF file must be 0.");
            }
            // Method tokens
            methodTokens = reader.readSerializableList(MethodToken.class);
            // Reserved bytes
            if (reader.readUInt16() != 0) {
                throw new DeserializationException("Reserve bytes in NEF file must be 0.");
            }
            // Script
            script = reader.readVarBytes(MAX_SCRIPT_LENGTH);
            if (script.length == 0) {
                throw new DeserializationException("Script can't be empty in NEF file.");
            }
            // Check sum
            checkSum = reader.readBytes(CHECKSUM_SIZE);
            if (!Arrays.equals(checkSum, computeChecksum(this))) {
                throw new DeserializationException("The checksums did not match");
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public static byte[] computeChecksum(NefFile file) {
        byte[] serialized = file.toArray();
        // Get nef file bytes without the checksum.
        int fileSizeWithoutCheckSum = serialized.length - CHECKSUM_SIZE;
        byte[] nefFileBytes = ArrayUtils.getFirstNBytes(serialized, fileSizeWithoutCheckSum);
        // Hash the nef file bytes and from that the first bytes as the checksum.
        return ArrayUtils.getFirstNBytes(Hash.hash256(nefFileBytes), CHECKSUM_SIZE);
    }

    public static NefFile readFromFile(File nefFile) throws DeserializationException, IOException {
        int nefFileSize = (int) nefFile.length();
        if (nefFileSize > 0x100000) {
            // This maximum size was taken from the neo-core code.
            throw new IllegalArgumentException("The given NEF file is too large. File was "
                    + nefFileSize + " bytes, but a max of 2^20 bytes is allowed.");
        }
        try (FileInputStream nefStream = new FileInputStream(nefFile)) {
            BinaryReader reader = new BinaryReader(nefStream);
            return reader.readSerializable(NefFile.class);
        }
    }

    public static NefFile fromStackItem(StackItem stackItem)
            throws DeserializationException, IOException {

        // the 'nef' is represented in a ByteString stack item
        if (!stackItem.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), StackItemType.BYTE_STRING);
        }
        ByteStringStackItem byteStringStackItem = stackItem.asByteString();
        byte[] nefBytes = byteStringStackItem.getValue();
        try (ByteArrayInputStream nefStream = new ByteArrayInputStream(nefBytes)) {
            BinaryReader reader = new BinaryReader(nefStream);
            return reader.readSerializable(NefFile.class);
        }
    }

    /**
     * Represents a static call to another contract from within a smart contract.
     * <p>
     * Method tokens are referenced in the smart contract's script whenever the referenced method is
     * called.
     */
    public static class MethodToken extends NeoSerializable {

        private static final int PARAMS_COUNT_SIZE = 2; // short
        private static final int HAS_RETURN_VALUE_SIZE = 1; // boolean
        private static final int CALL_FLAGS_SIZE = 1; // byte

        private ScriptHash hash;
        private String method;
        private int parametersCount;
        private boolean hasReturnValue;
        private CallFlagsType callFlags;

        public MethodToken(ScriptHash hash, String method, int parametersCount,
                boolean hasReturnValue,
                CallFlagsType callFlags) {
            this.hash = hash;
            this.method = method;
            this.parametersCount = parametersCount;
            this.hasReturnValue = hasReturnValue;
            this.callFlags = callFlags;
        }

        public MethodToken() {

        }

        public ScriptHash getHash() {
            return hash;
        }

        public String getMethod() {
            return method;
        }

        public int getParametersCount() {
            return parametersCount;
        }

        public boolean isHasReturnValue() {
            return hasReturnValue;
        }

        public CallFlagsType getCallFlags() {
            return callFlags;
        }

        @Override
        public void deserialize(BinaryReader reader) throws DeserializationException {
            try {
                hash = reader.readSerializable(ScriptHash.class);
                method = reader.readVarString();
                parametersCount = reader.readUInt16();
                hasReturnValue = reader.readBoolean();
                callFlags = CallFlagsType.valueOf(reader.readByte());
            } catch (IOException e) {
                throw new DeserializationException(e);
            }
        }

        @Override
        public void serialize(BinaryWriter writer) throws IOException {
            writer.writeSerializableFixed(hash);
            writer.writeVarString(method);
            writer.writeUInt16(parametersCount);
            writer.writeBoolean(hasReturnValue);
            writer.writeByte(callFlags.byteValue());
        }

        @Override
        public int getSize() {
            return NeoConstants.SCRIPTHASH_SIZE
                    + IOUtils.getVarSize(method)
                    + PARAMS_COUNT_SIZE
                    + HAS_RETURN_VALUE_SIZE
                    + CALL_FLAGS_SIZE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MethodToken)) {
                return false;
            }
            MethodToken that = (MethodToken) o;

            return parametersCount == that.parametersCount
                    && hasReturnValue == that.hasReturnValue
                    && hash.equals(that.hash) && method.equals(that.method)
                    && callFlags == that.callFlags;
        }

        @Override
        public int hashCode() {
            int result = hash != null ? hash.hashCode() : 0;
            result = 31 * result + (method != null ? method.hashCode() : 0);
            result = 31 * result + parametersCount;
            result = 31 * result + (hasReturnValue ? 1 : 0);
            result = 31 * result + (callFlags != null ? callFlags.hashCode() : 0);
            return result;
        }
    }
}
