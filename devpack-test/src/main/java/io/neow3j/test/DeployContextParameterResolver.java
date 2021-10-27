package io.neow3j.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static io.neow3j.test.ContractTestExtension.DEPLOY_CTX_STORE_KEY;

public class DeployContextParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {

        return parameterContext.getParameter().getType().equals(DeployContext.class);
    }

    @Override
    public DeployContext resolveParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        return (DeployContext) store.get(DEPLOY_CTX_STORE_KEY);
    }
}
