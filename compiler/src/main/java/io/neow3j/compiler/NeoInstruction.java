package io.neow3j.compiler;

import io.neow3j.constants.OpCode;
import io.neow3j.constants.OperandSize;
import io.neow3j.io.BinaryReader;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.IOException;

/**
 * Represents a single NeoVM opcode and possible operands that belong to the opcode.
 */
public class NeoInstruction {

    // The NeoVM opcode.
    private OpCode opcode;

    // One or multiple operands of variable byte size, joined together in one byte array.
    private byte[] operand;

    // Stores data depending on the type of this transaction. If this transaction is a method
    // call, this object should be the called `NeoMethod`.
    private Object extra;

    // The corresponding line number in the source code file that this instructions originates from.
    private Integer lineNr = null;

    // The NeoVM script address of this instruction in its respective method. This is not the
    // absolute address in the script, but only relative to the method.
    private int address;

    /**
     * Constructs a new instruction with the given opcode and operand. The operand needs to "fit"
     * the opcode, i.e., must have a length that is supported by the given opcode.
     *
     * @param opcode  the Neo opcode of this instruction.
     * @param operand the operand.
     */
    public NeoInstruction(OpCode opcode, byte[] operand) {
        this.opcode = opcode;
        checkOperandSize(operand);
        this.operand = operand;
    }

    /**
     * Constructs a new instruction with the given opcode and an empty operand.
     *
     * @param opcode the Neo opcode of this instruction.
     */
    public NeoInstruction(OpCode opcode) {
        this.opcode = opcode;
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
     * Gets the operand of this instruction if it has one. If this instruction doesn't have an
     * operand, an empty array is returned.
     *
     * @return the operand.
     */
    public byte[] getOperand() {
        return operand;
    }

    /**
     * Gets the line number in the source file that this instruction corresponds to.
     *
     * @return the line number.
     */
    public Integer getLineNr() {
        return lineNr;
    }

    /**
     * Gets this instruction's byte address, i.e., its position in the method it belongs to.
     * <p>
     * The address is absolute in the method that this instruction lives in but not in the whole
     * module.
     *
     * @return the address.
     */
    public int getAddress() {
        return address;
    }

    /**
     * Gets the extra information set on this instruction.
     *
     * @return the extra or null if no auxiliary data was set.
     */
    public Object getExtra() {
        return extra;
    }

    /**
     * Sets the given object as extra information on this instruction.
     * <p>
     * This is used, for example, for setting a reference to a method that this instruction jumps
     * to.
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
    byte[] toByteArray() {
        byte[] bytes = new byte[1 + operand.length];
        bytes[0] = (byte) opcode.getCode();
        System.arraycopy(operand, 0, bytes, 1, operand.length);
        return bytes;
    }

    /**
     * Returns the size of this instruction in bytes.
     *
     * @return The byte-size of this instruction.
     */
    int byteSize() {
        return 1 + operand.length;
    }

    @Override
    public String toString() {
        return opcode.toString() + " " + Numeric.toHexStringNoPrefix(operand);
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
     * Sets the given operand on this instruction. Overwrites if there was one set before. Checks if
     * the operand's size is supported by this instruction's opcode.
     *
     * @param operand The operand.
     */
    public void setOperand(byte[] operand) {
        checkOperandSize(operand);
        this.operand = operand;
    }

    // Checks if a given operand is compatible with this instructions opcode. This should help
    // to detect some errors when implementing the compiler.
    private void checkOperandSize(byte[] operand) {
        OperandSize operandSize = OpCode.getOperandSize(this.opcode);
        if (operandSize == null) {
            assert operand == null || operand.length == 0 : "Tried to set the operand "
                    + Numeric.toHexStringNoPrefix(operand) + " on opcode " + this.opcode.name()
                    + " but it doesn't take any operands.";
            return;
        }
        if (operandSize.prefixSize() > 0) {
            assert operandSize.prefixSize() == 1 || operandSize.prefixSize() == 2
                    || operandSize.prefixSize() == 4 : "Unexpected operand prefix size. Prefix "
                    + "size was " + operandSize.prefixSize() + " but only 1, 2, or 4 are expected.";
            assert operand.length >= operandSize.prefixSize() : "Opcode " + this.opcode.name()
                    + " needs a operand prefix of size " + operandSize.prefixSize()
                    + " but the given operand is only of size " + operand.length;
            byte[] prefix = ArrayUtils.getFirstNBytes(operand, operandSize.prefixSize());
            BinaryReader reader = new BinaryReader(prefix);
            int operandContentSize = 0;
            try {
                if (operandSize.prefixSize() == 1) {
                    operandContentSize = reader.readByte();
                } else if (operandSize.prefixSize() == 2) {
                    operandContentSize = reader.readShort();
                } else if (operandSize.prefixSize() == 4) {
                    operandContentSize = reader.readInt();
                }
            } catch (IOException ignore) {
            }
            assert operand.length == operandSize.prefixSize() + operandContentSize : "Operand "
                    + "prefix specified an operand size of " + operandContentSize + " but the "
                    + "operand had a different length.";
            return;
        }
        assert operand.length == operandSize.size() : "Operand " + Numeric.toHexStringNoPrefix(
                operand) + " has the wrong size for opcode " + this.opcode.name();
    }
}
