package io.neow3j.script;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define the size of OpCode operands and operand prefixes.
 * <p>
 * For example, {@link OpCode#PUSHDATA1} has a prefix size of 1 which means that before the operand there is one byte
 * prefix that determines how long the operand is. Or {@link OpCode#JMP} has an operand size of 1 byte which tells
 * the offset to jump.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperandSize {

    /**
     * @return the OpCode's operand prefix size.
     */
    int prefixSize() default 0;

    /**
     * @return the OpCode's operand size.
     */
    int size() default 0;

}
