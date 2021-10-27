package io.neow3j.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on static methods that return a {@link io.neow3j.types.ContractParameter}.
 * The parameter will be passed to the deployment of the contract mentioned in
 * {@link DeployConfig#value()}. The annotated method must be static, return a {@code
 * ContractParameter}, and either take no parameters or one parameter of type {@link DeployContext}.
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
