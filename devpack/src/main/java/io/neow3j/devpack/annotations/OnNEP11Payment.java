package io.neow3j.devpack.annotations;

import io.neow3j.devpack.Hash160;

@MethodSignature(
        name = "onNEP11Payment",
        parameterTypes = {Hash160.class, int.class, String.class},
        returnType = void.class
)
public @interface OnNEP11Payment {
}
