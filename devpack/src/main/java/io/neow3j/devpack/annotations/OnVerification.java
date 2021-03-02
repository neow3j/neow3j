package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * When used on a method of a smart contract class, this annotation signals that the annotated
 * method is to be used in the case that the contract is called for verification. The verification
 * method can have any name, take any number and type of arguments but must return a boolean.
 * <p>
 * The method will appear under the name {@code verify} in the contract manifest.
 * <p>
 * A verify method is required, for example, when a contract can receive tokens and those tokens
 * should be only spendable under certain conditions.
 */
@MethodSignature(
        name = "verify",
        parameterTypes = {},
        returnType = boolean.class
)
@Target(ElementType.METHOD)
public @interface OnVerification {

}
