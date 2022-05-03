package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * When used on a method of a smart contract class, this annotation signals that the annotated method is to be called
 * on deployment of the contract. Nodes will automatically call the method when the contract is deployd or updated
 * via the ContractManagement contract.
 * <p>
 * The annotated method can have any name but needs to have the signature "{@code void methodName (Object data,
 * boolean isUpdate)}". The method will appear under the name {@code _deploy} in the contract manifest.
 */

@MethodSignature(
        name = "_deploy",
        parameterTypes = {Object.class, boolean.class},
        returnType = void.class
)
@Target(ElementType.METHOD)
public @interface OnDeployment {

}
