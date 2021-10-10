package io.neow3j.test;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jExpress;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static io.neow3j.test.ContractTestExtension.NEOW3J_STORE_KEY;

public class Neow3jParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        return parameterContext.getParameter().getType().equals(Neow3j.class)
                || parameterContext.getParameter().getType().equals(Neow3jExpress.class);
    }

    @Override
    public Neow3jExpress resolveParameter(ParameterContext parameterContext,
            ExtensionContext context) throws ParameterResolutionException {

        return (Neow3jExpress) context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(NEOW3J_STORE_KEY);
    }
}
