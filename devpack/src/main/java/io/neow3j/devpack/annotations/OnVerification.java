package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Signals that the annotated method is to be used in the case that the contract is called for verification. The
 * verification method can have any name, take any number and type of arguments but must return a boolean. It must
 * not contain any events/notifications, or it will fail on every invocation.
 * <p>
 * The method will appear under the name {@code verify} in the contract manifest.
 * <p>
 * A verify method is required, for example, when you want to withdraw tokens from a contract. Inside the verify
 * method you usually check if the account trying to withdraw tokens is the contract owner.
 */
@MethodSignature(
        name = "verify",
        parameterTypes = {},
        returnType = boolean.class
)
@Target(ElementType.METHOD)
public @interface OnVerification {

}
