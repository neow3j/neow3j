package io.neow3j.devpack.framework.annotations;

import io.neow3j.devpack.framework.annotations.Syscall.Syscalls;
import java.lang.annotation.Repeatable;

@Repeatable(Syscalls.class)
public @interface Syscall {

    String value();

    @interface Syscalls {
        Syscall[] value();
    }
}

