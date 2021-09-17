package io.neow3j.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith({Neow3jParameterResolver.class,
        ContainerParameterResolver.class,
        ContractParameterResolver.class})
@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface ContractTest {

    /**
     * Sets the time interval between blocks for the neo-express instance on which tests are
     * executed. The block time can also be set via the default.neo-express file in the
     * {@code chainSecondsPerBlock} property. That property is overwritten when setting it here
     * on the annotation.
     */
    int blockTime() default 0;
}
