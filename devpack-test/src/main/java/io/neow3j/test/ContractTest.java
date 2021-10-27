package io.neow3j.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith({
        DeployContextParameterResolver.class,
        Neow3jParameterResolver.class,
        ContainerParameterResolver.class
})
@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface ContractTest {

    /**
     * Sets the time interval between blocks for the neo-express instance on which tests are
     * executed. The block time can also be set via the default.neo-express file in the
     * {@code chainSecondsPerBlock} property. That property is overwritten when setting it here
     * on the annotation.
     *
     * @return the block time.
     */
    int blockTime() default 0;

    /**
     * The contracts to compile and deploy before running the test instance.
     *
     * @return the contracts to compile and deploy
     */
    Class<?>[] contracts();

    /**
     * The batch file to run before all tests of the test instance. The batch file must be placed
     * in the resources directory.
     * <p>
     * The batch file can be combined with a checkpoint. In that case, the checkpoint is applied
     * first.
     *
     * @return the batch file name
     */
    String batchFile() default "";

    /**
     * The checkpoint file to apply before all tests of the test instance. The checkpoint file
     * must be placed in the resources directory.
     * <p>
     * The checkpoint can be combined with a batch file. In that case, the checkpoint is applied
     * first.
     *
     * @return the checkpoint file name.
     */
    String checkpoint() default "";

    /**
     * The neo-express configuration file to use. The file must be placed in the resources
     * directory.
     *
     * @return the configuration file.
     */
    String neoxpConfig() default "";
}
