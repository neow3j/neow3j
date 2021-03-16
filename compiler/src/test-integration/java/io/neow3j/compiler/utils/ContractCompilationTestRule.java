package io.neow3j.compiler.utils;

import static io.neow3j.compiler.ContractTest.setUp;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;

public class ContractCompilationTestRule implements TestRule {

    private final String fullyQualifiedClassName;
    private final GenericContainer<?> genericContainer;

    public ContractCompilationTestRule(String fullyQualifiedName,
            GenericContainer<?> genericContainer) {
        this.fullyQualifiedClassName = fullyQualifiedName;
        this.genericContainer = genericContainer;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUp(fullyQualifiedClassName, genericContainer);
                base.evaluate();
            }
        };
    }
}
