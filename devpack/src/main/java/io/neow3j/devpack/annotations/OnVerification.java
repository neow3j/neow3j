package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * When used on a method of a smart contract class, this annotation signals that the annotated
 * method is to be used in the case that the contract is called for verification. The
 * verification method - called {@code verify} in the contract's manifest - can take any
 * number and type of arguments and must return a boolean.
 *
 * The {@code verify} method is called when the contract's address is included in the signatures
 * of a transaction.
 */
@Target(ElementType.METHOD)
public @interface OnVerification {

}
