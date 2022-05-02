package io.neow3j.test;

import org.junit.jupiter.api.BeforeAll;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on methods in your test class to configure the deployment of a contract. Such a configuration
 * method must be static and return a {@link DeployConfiguration}. It can either take no parameters or one parameter
 * of type {@link DeployContext}. The contract configured with such a method is set in the
 * {@link DeployConfig#value()} of this annotation.
 * <p>
 * The annotated methods are called before a potential {@code setUp} method (annotated with {@link BeforeAll}). Thus,
 * you should not access objects set up in the {@code setUp} method. I you need access to things like accounts, set
 * them up in the static constructor of your test class.
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
