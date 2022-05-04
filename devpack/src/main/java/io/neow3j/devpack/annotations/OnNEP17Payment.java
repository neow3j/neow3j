package io.neow3j.devpack.annotations;

import io.neow3j.devpack.Hash160;

/**
 * Marks a smart contract method as the method to be called when the contract receives tokens from a NEP-17 contract.
 * The annotated method can have any name but is required to have the signature "{@code void methodName(Hash160
 * sender, int amount, Object data)}". The method will appear under the name "onNEP17Payment" in the contract manifest.
 * <p>
 * If a contract does not have this method, it cannot receive NEP-17 tokens.
 */
@MethodSignature(
        name = "onNEP17Payment",
        parameterTypes = {Hash160.class, int.class, Object.class},
        returnType = void.class
)
public @interface OnNEP17Payment {

}
