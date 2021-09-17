package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static io.neow3j.test.ContractTestExtension.CONTRACT_STORE_KEY;
import static io.neow3j.test.ContractTestExtension.NEOW3J_STORE_KEY;

public class ContractParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {

        return parameterContext.getParameter().getType().equals(SmartContract.class);
    }

    @Override
    public SmartContract resolveParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        Hash160 contractHash = (Hash160) store.get(CONTRACT_STORE_KEY);
        Neow3jExpress neow3j = (Neow3jExpress) store.get(NEOW3J_STORE_KEY);
        return new SmartContract(contractHash, neow3j);
    }
}
