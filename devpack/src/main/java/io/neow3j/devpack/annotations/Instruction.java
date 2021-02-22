package io.neow3j.devpack.annotations;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction.Instructions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Used to mark a method to be replaced with an {@link OpCode} and its operand. The method can then
 * be used in a smart contract. A method can be annotated with multiple <tt>Instructions</tt>, which
 * offers the possibility to create a short, static script that is inserted wherever the annotated
 * method is called.
 * <p>
 * The method's body is ignored by the NeoVM compiler if it has this annotation.
 */
@Repeatable(Instructions.class)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Instruction {

    OpCode opcode() default OpCode.NOP;

    byte[] operandPrefix() default {};

    byte[] operand() default {};

    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Instructions {

        Instruction[] value();
    }
}
