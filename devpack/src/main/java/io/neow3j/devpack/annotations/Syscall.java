package io.neow3j.devpack.annotations;

import io.neow3j.script.InteropService;
import io.neow3j.devpack.annotations.Syscall.Syscalls;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;


/**
 * Used to mark a method to be replaced with a specific {@link InteropService}. The annotated
 * method can be used in a smart contract. A method can be annotated with multiple
 * {@code Syscalls}, which will be invoked sequentially.
 * <p>
 * The method's body is ignored by the NeoVM compiler if it has this annotation.
 */
@Repeatable(Syscalls.class)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Syscall {

    InteropService value();

    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Syscalls {

        Syscall[] value();
    }
}

