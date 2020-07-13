package io.neow3j.devpack.framework.annotations;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.framework.annotations.Syscall.Syscalls;
import java.lang.annotation.Repeatable;

@Repeatable(Syscalls.class)
public @interface Syscall {

    InteropServiceCode value();

    @interface Syscalls {
        Syscall[] value();
    }
}

