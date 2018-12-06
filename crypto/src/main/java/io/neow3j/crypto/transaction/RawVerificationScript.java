package io.neow3j.crypto.transaction;

import io.neow3j.constants.OpCode;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RawVerificationScript extends NeoSerializable {

    private List<BigInteger> publicKeys;

    private int amountSignatures;

    public RawVerificationScript() {
    }

    public RawVerificationScript(List<BigInteger> publicKeys, int amountSignatures) {
        this.publicKeys = publicKeys;
        this.amountSignatures = amountSignatures;
    }

    public RawVerificationScript(List<BigInteger> publicKeys) {
        this(publicKeys, 0);
    }

    public List<BigInteger> getPublicKeys() {
        return publicKeys;
    }

    public int getAmountSignatures() {
        return amountSignatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawVerificationScript)) return false;
        RawVerificationScript that = (RawVerificationScript) o;
        return getAmountSignatures() == that.getAmountSignatures() &&
                Objects.equals(getPublicKeys(), that.getPublicKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPublicKeys(), getAmountSignatures());
    }

    @Override
    public String toString() {
        return "VerificationScript{" +
                "publicKeys=" + publicKeys +
                ", amountSignatures=" + amountSignatures +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        long scriptLength = reader.readVarInt();
        int initialPosition = reader.getPosition();
        int bytesRead = 0;

        // look ahead for checking whether the PUSH opcode
        // to specify min amount of signatures is set or not
        reader.mark(Byte.BYTES);
        byte firstByte = reader.readByte();
        if (isPushOpcodeForAmountOfSignaturesValueIsSet(firstByte)) {
            this.amountSignatures = (firstByte) - 0x50;
            bytesRead = reader.getPosition() - initialPosition;
        } else {
            reader.reset();
        }

        this.publicKeys = new ArrayList<>();
        while (bytesRead < scriptLength) {
            byte length = reader.readByte();
            if (!isCheckSigOpcodeValueIsSet(length) && !isPushOpcodeForAmountOfSignaturesValueIsSet(length)) {
                byte[] pubKey = reader.readBytes(length);
                this.publicKeys.add(Numeric.toBigInt(pubKey));
            }
            bytesRead = reader.getPosition() - initialPosition;
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        if (publicKeys.size() > 1 && amountSignatures != 0) {
            writer.writeByte((byte) (amountSignatures + 0x50));
        }
        for (int i = 0; i < publicKeys.size(); i++) {
            byte[] pubKeyByteArray = publicKeys.get(i).toByteArray();
            writer.writeByte((byte) pubKeyByteArray.length);
            writer.write(pubKeyByteArray);
        }
        if (publicKeys.size() > 1) {
            writer.writeByte((byte) (publicKeys.size() + 0x50));
            writer.writeByte(OpCode.CHECKMULTISIG.getValue());
        } else {
            writer.writeByte(OpCode.CHECKSIG.getValue());
        }
    }

    private boolean isPushOpcodeForAmountOfSignaturesValueIsSet(byte byteRead) {
        if (byteRead >= OpCode.PUSH1.getValue() && byteRead <= OpCode.PUSH16.getValue()) {
            return true;
        }
        return false;
    }

    private boolean isCheckSigOpcodeValueIsSet(byte byteRead) {
        if (byteRead == OpCode.CHECKSIG.getValue() || byteRead == OpCode.CHECKMULTISIG.getValue()) {
            return true;
        }
        return false;
    }
}
