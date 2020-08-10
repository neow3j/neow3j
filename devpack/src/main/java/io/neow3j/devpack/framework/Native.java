package io.neow3j.devpack.framework;

import io.neow3j.devpack.framework.annotations.Appcall;

/**
 * Provides convenience methods to call Neo-native contracts (NEO, GAS, and Policy)
 */
// TODO: Update script hashes if native contracts change.
public class Native {

    /**
     * Calls the NEO token contract with the given method and arguments.
     * <p>
     * It is up to the developer to cast the result to the correct type.
     *
     * @param method    The method to call.
     * @param arguments The arguments to hand to the method.
     * @return the result of the execution.
     */
    @Appcall(scriptHash = "0xde5f57d430d3dece511cf975a8d37848cb9e0525")
    public static native Object callNeoContract(String method, Object[] arguments);

    /**
     * Calls the GAS token contract with the given method and arguments.
     * <p>
     * It is up to the developer to cast the result to the correct type.
     *
     * @param method    The method to call.
     * @param arguments The arguments to hand to the method.
     * @return the result of the execution.
     */
    @Appcall(scriptHash = "0x668e0c1f9d7b70a99dd9e06eadd4c784d641afbc")
    public static native Object callGasContract(String method, Object... arguments);

    /**
     * Calls the policy contract with the given method and arguments.
     * <p>
     * It is up to the developer to cast the result to the correct type.
     *
     * @param method    The method to call.
     * @param arguments The arguments to hand to the method.
     * @return the result of the execution.
     */
    @Appcall(scriptHash = "0xce06595079cd69583126dbfd1d2e25cca74cffe9")
    public static native Object callPolicyContract(String method, Object... arguments);

}
