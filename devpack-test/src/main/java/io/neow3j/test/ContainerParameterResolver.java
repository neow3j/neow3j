package io.neow3j.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static io.neow3j.test.ContractTestExtension.CHAIN_STORE_KEY;

public class ContainerParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        return parameterContext.getParameter().getType().equals(NeoExpressTestContainer.class);
    }

    @Override
    public NeoExpressTestContainer resolveParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        return (NeoExpressTestContainer) context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(CHAIN_STORE_KEY);
    }
}
