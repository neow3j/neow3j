package io.neow3j.test;

import org.junit.jupiter.api.BeforeAll;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on static methods that return a {@link io.neow3j.types.ContractParameter}.
 * The parameter will be passed to the deployment of the contract mentioned in
 * {@link DeployConfig#value()}. The annotated method must be static, return a {@code
 * ContractParameter}, and either take no parameters or one parameter of type {@link DeployContext}.
 * <p>
 * The annotated methods are called before a potential {@code setUp} method (annotated with
 * {@link BeforeAll}). Thus, you should not access objects set up in the {@code setUp} method.
 * Furthermore, the {@link ContractTestExtension} object is not in a consistent state when a deploy
 * config method is called, i.e., don't access the {@link ContractTestExtension} in the deploy
 * config method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeployConfig {

    /**
     * The contract class that the deployment configuration is meant for.
     *
     * @return the contract class.
     */
    Class<?> value();

}
