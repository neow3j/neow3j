package io.neow3j.devpack.annotations;

import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.devpack.annotations.Instruction.Instructions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Represents one NeoVM instruction and can be used as such on methods and constructors. Each
 * instance of this annotation is converted into an instruction in the final VM script.
 * A method can be annotated with multiple {@code Instructions}, which offers the possibility to
 * create static script that is inserted wherever the annotated method is called.
 * <p>
 * If a method has this annotation its body is ignored, thus, it makes sense to use the
 * {@code native} qualifier in such a method's signature.
 */
@Repeatable(Instructions.class)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Instruction {

    /**
     * The NeoVM opcode to use in this instruction.
     */
    OpCode opcode() default OpCode.NOP;

    /**
     * If the operand can have a variable size, this specifies the operand size.
     */
    byte[] operandPrefix() default {};

    /**
     * The instruction's operand.
     */
    byte[] operand() default {};

    /**
     * If the OpCode is a {@link OpCode#SYSCALL}, set this property to an {@link InteropService}
     * and ignore {@link Instruction#opcode()}, {@link Instruction#operand()} and
     * {@link Instruction#operandPrefix()}.
     */
    InteropService interopService() default InteropService.DUMMY;

    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Instructions {

        Instruction[] value();
    }

}
