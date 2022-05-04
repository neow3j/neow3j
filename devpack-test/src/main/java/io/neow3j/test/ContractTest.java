package io.neow3j.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface ContractTest {

    /**
     * Sets the time interval between blocks for the blockchain instance on which tests are executed. This overwrites
     * block time values set in the configuration passed via {@link ContractTest#configFile()}.
     *
     * @return the block time.
     */
    int blockTime() default 0;

    /**
     * The contracts to compile and deploy before running the test instance.
     * <p>
     * Note that the order of the contracts here is the order in which the contracts will be deployed. If you use
     * deployment configuration methods (annotated with {@link DeployConfig}) in your test class that require
     * information of another deployed contract, beware that a contract is only available if it is in a previous
     * position in the deployment order.
     *
     * @return the contracts to compile and deploy.
     */
    Class<?>[] contracts();

    /**
     * The batch file to run before all tests of the test instance. The batch file must be placed in the resources
     * directory.
     * <p>
     * The batch file can be combined with a checkpoint. In that case, the checkpoint is applied first.
     *
     * @return the batch file name.
     */
    String batchFile() default "";

    /**
     * The checkpoint file to apply before all tests of the test instance. The checkpoint file must be placed in the
     * resources directory.
     * <p>
     * The checkpoint can be combined with a batch file. In that case, the checkpoint is applied first.
     *
     * @return the checkpoint file name.
     */
    String checkpoint() default "";

    /**
     * The configuration file to use for configuring the test blockchain. The file must be placed in the resources
     * directory.
     *
     * @return the configuration file.
     */
    String configFile() default "";

}
