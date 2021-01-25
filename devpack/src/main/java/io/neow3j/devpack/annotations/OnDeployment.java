package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * When used on a method of a smart contract class, this annotation signals that the annotated
 * method is to be called on deployment of the contract. Nodes will automatically call the method
 * when it is deployd.
 * <p>
 * The deployment method - named {@code _deploy} in the contract's manifest - needs to have a
 * boolean parameter and {@code void} as its return type.
 * <p>
 * This annotation can only be used on a {@code public static} method of a smart contract class,
 * and not on other classes that provide functionality for the contract.
 */
@Target(ElementType.METHOD)
public @interface OnDeployment {

}
