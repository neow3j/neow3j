package io.neow3j.contract;

import static io.neow3j.utils.ArrayUtils.trimTrailingBytes;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    // NEO Executable Format 3 (NEF3)
    private static final int MAGIC = 0x3346454E; // 860243278 in decimal
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
        checkSum = new byte[]{};
        script = new byte[]{};
    }

    public NefFile(String compiler, String version, byte[] script) {
        int compilerSize = compiler.getBytes(UTF_8).length;
        if (compilerSize > COMPILER_SIZE) {
            throw new IllegalArgumentException(format("The compiler name and version string can "
                    + "be max %d bytes long, but was %d bytes long.", COMPILER_SIZE, compilerSize));
        }
        this.compiler = compiler;
        this.script = script;
        // Need to initialize the check sum because it is required for calculating the check sum.
        checkSum = new byte[CHECKSUM_SIZE];
        checkSum = computeChecksum(this);
    }

    /**
     * Gets the compiler (and version) with which this NEF file has been generated.
     * @return the compiler name and version.
     */
    public String getCompiler() {
        return compiler;
    }

    /**
     * Gets the contract's method tokens.
     * <p>
     * The tokens represent calls to other contracts.
     * @return the contract's method tokens.
     */
    public List<MethodToken> getMethodTokens() {
        return methodTokens;
    }

    /**
     * Gets the contract script.
     * @return the contract script.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * Gets this NEF file's check sum.
     * @return the check sum.
     */
    public byte[] getCheckSum() {
        return checkSum;
    }

    /**
     * Gets the byte size of this NEF file when serialized.
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
            long l = reader.readUInt32();
            if (l != MAGIC) {
                throw new DeserializationException("Wrong magic number in NEF file.");
            }

            // Compiler
            byte[] compilerBytes = reader.readBytes(COMPILER_SIZE);
            compiler = new String(trimTrailingBytes(compilerBytes, (byte) 0), UTF_8);

            // Reserved bytes
            if (reader.readUInt16() != 0) {
                throw new DeserializationException("Reserve bytes in NEF file must be 0.");
            }

            script = reader.readVarBytes(MAX_SCRIPT_LENGTH);
            if (script.length == 0) {
                throw new DeserializationException("Script can't be empty in NEF file.");
            }
            checkSum = reader.readBytes(CHECKSUM_SIZE);
            if (!Arrays.equals(checkSum, computeChecksum(this))) {
                throw new DeserializationException("The checksums did not match");
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public static byte[] computeChecksum(NefFile file) {
        byte[] serialized = new byte[]{};
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            BinaryWriter writer = new BinaryWriter(stream);
            writer.writeSerializableFixed(file);
            serialized = stream.toByteArray();
        } catch (IOException e) {
            // Doesn't happen because we're not writing to anywhere.
        }
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
}
