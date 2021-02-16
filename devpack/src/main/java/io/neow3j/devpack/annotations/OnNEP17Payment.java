package io.neow3j.devpack.annotations;

import io.neow3j.devpack.Hash160;

@MethodSignature(
        name = "onNEP17Payment",
        parameterTypes = {Hash160.class, int.class, Object.class},
        returnType = void.class
)
public @interface OnNEP17Payment {
}
