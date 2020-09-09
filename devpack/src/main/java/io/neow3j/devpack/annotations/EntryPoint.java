package io.neow3j.devpack.annotations;

import java.lang.annotation.*;

/**
 * Used to mark the method that is the default entry point of a smart contract. It can only be used
 * on one method of the contract, otherwise the compilation will fail.
 */
@Target(ElementType.METHOD)
public @interface EntryPoint {

}