package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import io.neow3j.script.OperandSize;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.utils.Numeric;

import java.io.IOException;

import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;

/**
 * Represents a single NeoVM opcode and possible operands that belong to the opcode.
 */
public class NeoInstruction {

    // The NeoVM opcode.
    private OpCode opcode;

    // The (optional) prefix of the instruction's operand usually determining the length of the operand.
    private byte[] operandPrefix;

    // The operand of the instruction.
    private byte[] operand;

    // Stores data depending on the type of this transaction. If this transaction is a method call, this object
    // should be the called `NeoMethod`.
    private Object extra;

    // The corresponding line number in the source code file that this instructions originates from.
    private Integer lineNr = null;

    // The NeoVM script address of this instruction in its respective method. This is not the absolute address in the
    // script, but only relative to the method.
    private int address;

    /**
     * Constructs a new instruction with the given opcode and operand. The operand needs to "fit" the opcode, i.e.,
     * must have a length that is supported by the given opcode.
     *
     * @param opcode  the Neo opcode of this instruction.
     * @param operand the operand.
     */
    public NeoInstruction(OpCode opcode, byte[] operand) {
        this(opcode, new byte[]{}, operand);
    }

    /**
     * Constructs a new instruction with the given opcode, operand prefix, and operand. The operand prefix states the
     * length of the following operand.
     *
     * @param opcode        the Neo opcode of this instruction.
     * @param operandPrefix the operand prefix.
     * @param operand       the operand.
     */
    public NeoInstruction(OpCode opcode, byte[] operandPrefix, byte[] operand) {
        this.opcode = opcode;
        checkOperandSize(operandPrefix, operand);
        this.operandPrefix = operandPrefix;
        this.operand = operand;
    }

    /**
     * Constructs a new instruction with the given opcode and an empty operand.
     *
     * @param opcode the Neo opcode of this instruction.
     */
    public NeoInstruction(OpCode opcode) {
        this.opcode = opcode;
        this.operandPrefix = new byte[]{};
        this.operand = new byte[]{};
    }

    /**
     * Gets the opcode of this instruction.
     *
     * @return the opcode.
     */
    public OpCode getOpcode() {
        return opcode;
    }

    /**
     * Gets the operand prefix of this instruction if it has one. If this instruction doesn't have an operand prefix,
     * an empty array is returned.
     *
     * @return the operand.
     */
    public byte[] getOperandPrefix() {
        return operandPrefix;
    }

    /**
     * Gets the operand of this instruction if it has one. If this instruction doesn't have an operand, an empty
     * array is returned.
     *
     * @return the operand.
     */
    public byte[] getOperand() {
        return operand;
    }

    /**
     * @return the line number in the source file that this instruction corresponds to.
     */
    public Integer getLineNr() {
        return lineNr;
    }

    /**
     * Gets this instruction's byte address, i.e., its position in the method it belongs to.
     * <p>
     * The address is absolute in the method that this instruction lives in but not in the whole module.
     *
     * @return the address.
     */
    public int getAddress() {
        return address;
    }

    /**
     * Gets the extra information set on this instruction. If no auxiliary data was set, null is returned.
     *
     * @return the extra information.
     */
    public Object getExtra() {
        return extra;
    }

    /**
     * Sets the given object as extra information on this instruction.
     * <p>
     * This is used, for example, for setting a reference to a method that this instruction jumps to.
     *
     * @param extra the extra information.
     * @return this.
     */
    public NeoInstruction setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    /**
     * Produces a consecutive array of bytes of this instruction's opcode and operands.
     *
     * @return the instruction as a byte array.
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[1 + operandPrefix.length + operand.length];
        bytes[0] = (byte) opcode.getCode();
        System.arraycopy(operandPrefix, 0, bytes, 1, operandPrefix.length);
        System.arraycopy(operand, 0, bytes, 1 + operandPrefix.length, operand.length);
        return bytes;
    }

    /**
     * @return the byte-size of this instruction.
     */
    public int byteSize() {
        return 1 + operandPrefix.length + operand.length;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + toHexStringNoPrefix(operandPrefix) + " " + toHexStringNoPrefix(operand);
    }

    public void setOpcode(OpCode opcode) {
        this.opcode = opcode;
    }

    void setLineNr(Integer lineNr) {
        this.lineNr = lineNr;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Sets the given operand on this instruction. Overwrites if there was one set before. Checks if the operand
     * prefix and operand's size are supported by this instruction's opcode.
     *
     * @param operandPrefix the operand prefix.
     * @param operand       the operand.
     */
    public void setOperand(byte[] operandPrefix, byte[] operand) {
        checkOperandSize(operandPrefix, operand);
        this.operandPrefix = operandPrefix;
        this.operand = operand;
    }

    /**
     * Sets the given operand on this instruction. Overwrites if there was one set before. Checks if the operand's
     * size is supported by this instruction's opcode.
     *
     * @param operand the operand.
     */
    public void setOperand(byte[] operand) {
        setOperand(new byte[]{}, operand);
    }

    // Checks if a given operand and its prefix are compatible with this instruction's opcode.
    private void checkOperandSize(byte[] operandPrefix, byte[] operand) {
        OperandSize operandSize = OpCode.getOperandSize(opcode);

        // Opcode does not take an operand
        if (operandSize == null && (operand == null || operand.length == 0)) {
            return;
        }

        // Opcode does not take an operand but an operand was specified.
        if (operandSize == null && operand != null && operand.length != 0) {
            throw new CompilerException(format("Tried to set an operand (%s) on the opcode '%s' which doesn't take " +
                    "any operands.", toHexStringNoPrefix(operand), opcode.name()));
        }

        // Opcode takes an operand but no operand was specified.
        if (operandSize != null && operand == null) {
            throw new CompilerException(format("Opcode '%s' requires an operand but no operand was specified.",
                    opcode.name()));
        }

        // Opcode takes an operand and no operand prefix but the specified operand has the wrong size.
        if (operandSize.prefixSize() == 0 && operand.length != operandSize.size()) {
            throw new CompilerException(format("Tried to set an operand (%s) with size %d on opcode '%s' which only " +
                            "takes operands of size %d.", toHexStringNoPrefix(operand), operand.length,
                    this.opcode.name(), operandSize.size()));
        }

        if (operandSize.prefixSize() > 0) {
            // Opcode takes an operand prefix but the prefix size is not of one of the allowed values.
            if (operandSize.prefixSize() != 1 && operandSize.prefixSize() != 2 && operandSize.prefixSize() != 4) {
                throw new CompilerException(format("Unexpected operand prefix size. Size was %d but only 1, 2, or 4 " +
                        "are allowed.", operandSize.prefixSize()));
            }

            // Opcode takes an operand prefix but the given prefix is not of the required length.
            if (operandPrefix.length != operandSize.prefixSize()) {
                throw new CompilerException(format("Opcode '%s' needs an operand prefix of size %d but the given " +
                                "operand prefix is of size %d.", opcode.name(), operandSize.prefixSize(),
                        operandPrefix.length));
            }

            // Check if operand has correct length according to specified operand prefix.
            BinaryReader reader = new BinaryReader(operandPrefix);
            long specifiedOperandSize = 0;
            try {
                if (operandSize.prefixSize() == 1) {
                    specifiedOperandSize = reader.readUnsignedByte();
                } else if (operandSize.prefixSize() == 2) {
                    specifiedOperandSize = reader.readUInt16();
                } else if (operandSize.prefixSize() == 4) {
                    specifiedOperandSize = reader.readUInt32();
                }
            } catch (IOException ignore) {
            }
            if (operand.length != specifiedOperandSize) {
                throw new CompilerException(format("Operand prefix specified an operand size of %d but the operand " +
                                "was %d bytes long.", specifiedOperandSize, operand.length));
            }
        }
    }

}
