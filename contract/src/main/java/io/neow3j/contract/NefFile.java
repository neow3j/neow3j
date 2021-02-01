package io.neow3j.contract;

import static java.lang.String.format;

import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;

import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * +------------+-----------+------------------------------------------------------------+
 * |   Field    |  Length   |                          Comment                           |
 * +------------+-----------+------------------------------------------------------------+
 * | Magic      | 4 bytes   | Magic header                                               |
 * | Compiler   | 32 bytes  | Compiler used                                              |
 * | Version    | 32 bytes  | Compiler version                                           |
 * +------------+-----------+------------------------------------------------------------+
 * | Script     | Var bytes | Var bytes for the payload                                  |
 * +------------+-----------+------------------------------------------------------------+
 * | Checksum   | 4 bytes   | First four bytes of double SHA256 hash                     |
 * +------------+-----------+------------------------------------------------------------+
 */
public class NefFile extends NeoSerializable {

    // NEO Executable Format 3 (NEF3)
    private static final int MAGIC = 0x3346454E; // 860243278 in decimal
    private static final int MAGIC_SIZE = 4;
    private static final int COMPILER_SIZE = 32;
    private static final int VERSION_SIZE = 32;
    private static final int MAX_SCRIPT_LENGTH = 512 * 1024;
    private static final int CHECKSUM_SIZE = 4;

    private static final int HEADER_SIZE = MAGIC_SIZE   // Magic (uint32)
            + COMPILER_SIZE                             // Compiler (32 bytes)
            + VERSION_SIZE;                             // Version (32 bytes)

    private String compiler;
    private String version;
    public byte[] checkSum; // 4 bytes. A uint in neo-core
    public byte[] script;

    public NefFile() {
        checkSum = new byte[]{};
        script = new byte[]{};
    }

    public NefFile(String compiler, String version, byte[] script) {
        int compilerNameSize = compiler.getBytes(StandardCharsets.UTF_8).length;
        if (compilerNameSize > COMPILER_SIZE) {
            throw new IllegalArgumentException(format("The compiler name can be max %d bytes long, "
                    + "but was %d bytes long.", COMPILER_SIZE, compilerNameSize));
        }
        this.compiler = compiler;

        int versionSize = version.getBytes(StandardCharsets.UTF_8).length;
        if (versionSize > VERSION_SIZE) {
            throw new IllegalArgumentException(format("The version string can be max %d bytes "
                    + "long, but was %d bytes long.", VERSION_SIZE, versionSize));
        }
        this.version = version;

        this.script = script;
        // Need to initialize the check sum because it is required for calculating the check sum.
        checkSum = new byte[CHECKSUM_SIZE];
        checkSum = computeChecksum(this);
    }

    public String getCompiler() {
        return compiler;
    }

    public String getVersion() {
        return version;
    }

    public byte[] getScript() {
        return script;
    }

    public byte[] getCheckSum() {
        return checkSum;
    }

    public long getCheckSumAsInteger() {
        return Numeric.toBigInt(checkSum).longValue();
    }

    @Override
    public int getSize() {
        return HEADER_SIZE
                + IOUtils.getVarSize(script)
                + CHECKSUM_SIZE;
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeUInt32(MAGIC);
        writer.writeFixedString(compiler, COMPILER_SIZE);
        writer.writeFixedString(version, VERSION_SIZE);
        writer.writeVarBytes(script);
        writer.write(checkSum);
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            long l = reader.readUInt32();
            if (l != MAGIC) {
                throw new DeserializationException("Wrong magic number in NEF file.");
            }
            byte[] compilerBytes = ArrayUtils.trimTrailingBytes(reader.readBytes(COMPILER_SIZE), (byte) 0);
            compiler = new String(compilerBytes, StandardCharsets.UTF_8);
            byte[] versionBytes = ArrayUtils.trimTrailingBytes(reader.readBytes(VERSION_SIZE), (byte) 0);
            version = new String(versionBytes, StandardCharsets.UTF_8);
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
