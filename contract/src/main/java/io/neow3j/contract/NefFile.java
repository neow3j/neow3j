package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
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
import java.util.Objects;

/**
 * +------------+-----------+------------------------------------------------------------+
 * |   Field    |  Length   |                          Comment                           |
 * +------------+-----------+------------------------------------------------------------+
 * | Magic      | 4 bytes   | Magic header                                               |
 * | Compiler   | 32 bytes  | Compiler used                                              |
 * | Version    | 16 bytes  | Compiler version (Mayor, Minor, Build, Version)            |
 * | ScriptHash | 20 bytes  | ScriptHash for the script                                  |
 * +------------+-----------+------------------------------------------------------------+
 * | Checksum   | 4 bytes   | Sha256 of the header (CRC)                                 |
 * +------------+-----------+------------------------------------------------------------+
 * | Script     | Var bytes | Var bytes for the payload                                  |
 * +------------+-----------+------------------------------------------------------------+
 */
public class NefFile extends NeoSerializable {

    // NEO Executable Format 3 (NEF3)
    private static final int MAGIC = 0x3346454E; // 860243278 in decimal

    private static final int HEADER_SIZE = 4    // Magic (uint32)
            + 32                                // Compiler (32 bytes String)
            + (4 * 4)                           // Version (4 * int32)
            + NeoConstants.SCRIPTHASH_SIZE      // Script hash
            + 4;                                // Checksum

    private String compiler;
    private Version version;
    public ScriptHash scriptHash;
    public byte[] checkSum; // 4 bytes. A uint in neo-core
    public byte[] script;

    public NefFile() {
        this.checkSum = new byte[]{};
        this.script = new byte[]{};
    }

    public NefFile(String compiler, Version version, byte[] script) {
        this.compiler = compiler;
        this.version = version;
        this.script = script;
        this.scriptHash = ScriptHash.fromScript(script);
        // Need to initialize the check sum because it is required for calculating the check sum.
        this.checkSum = new byte[]{};
        this.checkSum = computeChecksum(this);
    }

    public String getCompiler() {
        return compiler;
    }

    public Version getVersion() {
        return version;
    }

    public ScriptHash getScriptHash() {
        return scriptHash;
    }

    public byte[] getCheckSum() {
        return checkSum;
    }

    public byte[] getScript() {
        return script;
    }

    @Override
    public int getSize() {
        return HEADER_SIZE + IOUtils.getVarSize(this.script);
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeUInt32(MAGIC);
        writer.writeFixedString(this.compiler, 32);
        writer.writeSerializableFixed(this.version);
        writer.writeSerializableFixed(this.scriptHash);
        writer.write(this.checkSum);
        writer.writeVarBytes(this.script);
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            long l = reader.readUInt32();
            if (l != MAGIC) {
                throw new DeserializationException("Wrong magic number in NEF file.");
            }
            byte[] compilerBytes = ArrayUtils.trimTrailingBytes(reader.readBytes(32), (byte)0);
            this.compiler = new String(compilerBytes, StandardCharsets.UTF_8);
            this.version = reader.readSerializable(Version.class);
            this.scriptHash = reader.readSerializable(ScriptHash.class);
            this.checkSum = reader.readBytes(4);
            if (!Arrays.equals(this.checkSum, computeChecksum(this))) {
                throw new DeserializationException("The checksums did not match");
            }
            this.script = reader.readVarBytes(1024 * 1024);
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
        // Get header without the checksum.
        byte[] header = ArrayUtils.getFirstNBytes(serialized, HEADER_SIZE - 4);
        return ArrayUtils.getFirstNBytes(Hash.hash256(header), 4);
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

    public static class Version extends NeoSerializable {

        private int major;
        private int minor;
        private int build;
        private int revision;

        public Version() {

        }

        public Version(int major, int minor, int build, int revision) {
            this.major = major;
            this.minor = minor;
            this.build = build;
            this.revision = revision;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getBuild() {
            return build;
        }

        public int getRevision() {
            return revision;
        }

        @Override
        public void deserialize(BinaryReader reader) throws DeserializationException {
            try {
                this.major = reader.readInt();
                this.minor = reader.readInt();
                this.build = reader.readInt();
                this.revision = reader.readInt();
            } catch (IOException e) {
                throw new DeserializationException(e);
            }
        }

        @Override
        public void serialize(BinaryWriter writer) throws IOException {
            writer.writeInt32(this.major);
            writer.writeInt32(this.minor);
            writer.writeInt32(this.build);
            writer.writeInt32(this.revision);
        }

        @Override
        public int getSize() {
            return 32 * 4;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Version)) {
                return false;
            }
            Version version = (Version) o;
            return major == version.major
                    && minor == version.minor
                    && build == version.build
                    && revision == version.revision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor, build, revision);
        }
    }
}
