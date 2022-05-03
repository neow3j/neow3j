package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Represents one NeoVM instruction and can be used as such on methods and constructors. Each instance of this
 * annotation is converted into an instruction in the final VM script. A method can be annotated with multiple {@code
 * Instructions}, which offers the possibility to create static script that is inserted wherever the annotated method
 * is called.
 * <p>
 * If a method has this annotation its body is ignored, thus, it makes sense to use the {@code native} qualifier in
 * such a method's signature.
 * <p>
 * This annotation can only be used on methods and constructors of classes that are not main contract classes. I.e.,
 * you contract class cannot have empty methods that are annotated with this. Your contract class can make use of
 * other classes that have annotated methods. Those methods, or rather the instructions given in the annotations, are
 * then inlined in the code that called them.
 */
@Repeatable(Instructions.class)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Instruction {

    /**
     * @return the NeoVM opcode to use in this instruction.
     */
    OpCode opcode() default OpCode.NOP;

    /**
     * If the operand can have a variable size, this specifies the operand size.
     *
     * @return the operand prefix to use in this instruction.
     */
    byte[] operandPrefix() default {};

    /**
     * @return the operand to use for this instruction.
     */
    byte[] operand() default {};

    /**
     * If the OpCode is a {@link OpCode#SYSCALL}, set this property to an {@link InteropService} and ignore
     * {@link Instruction#opcode()}, {@link Instruction#operand()} and {@link Instruction#operandPrefix()}.
     * <p>
     * When calling an interop service, the required arguments have to be passed in the reverse order as they appear
     * in the interop service's method signature. E.g., for {@link InteropService#SYSTEM_RUNTIME_NOTIFY}, the
     * parameters are an event name and an array that represents the state to be passed with the call. To
     * successfully do this syscall the Instructions have to be ordered like this:
     * <pre>
     * {@code @Instruction(opcode = OpCode.NEWARRAY0)}
     * {@code @Instruction(opcode = OpCode.PUSHDATA1, operandPrefix = {0x02}, operand = {0x01, 0x02})}
     * {@code @Instruction(interopService = InteropService.SYSTEM_RUNTIME_NOTIFY)}
     * {@code public static void method() {...}}
     * </pre>
     * If you only use one single instruction annotation that holds a syscall, the compiler will take care of
     * reversing the arguments automatically. E.g., the following will work without having to add an instruction for
     * reversing the parameters.
     * <pre>
     * {@code @Instruction(interopService = InteropService.SYSTEM_RUNTIME_NOTIFY)}
     * {@code public static void method(String eventName, Object[] state) {...}}
     * </pre>
     *
     * @return the interop service set for this instruction.
     */
    InteropService interopService() default InteropService.DUMMY;

    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Instructions {

        Instruction[] value();

    }

}
