package io.neow3j.test;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class NeoContainerReadinessIntegrationTest {

    @Test
    public void neoContainerIsReadyWhenStartReturns() throws Exception {
        try (NeoTestContainer container = new NeoTestContainer()) {
            container.start();
            Neow3j neow3j = Neow3j.build(new HttpService(container.getNodeUrl()));
            try {
                assertThat(neow3j.getBlockCount().send().getBlockCount(), greaterThan(BigInteger.ZERO));
            } finally {
                neow3j.shutdown();
            }
        }
    }

    @Test
    public void neoExpressIsReadyWhenStartAndResumeReturn() throws Exception {
        try (NeoExpressTestContainer container = new NeoExpressTestContainer().withSecondsPerBlock(60)) {
            container.start();
            Neow3j neow3j = Neow3j.build(new HttpService(container.getNodeUrl()));
            try {
                assertThat(neow3j.getBlockCount().send().getBlockCount(), greaterThan(BigInteger.ZERO));

                container.halt();
                container.resume();

                assertThat(neow3j.getBlockCount().send().getBlockCount(), greaterThan(BigInteger.ZERO));
            } finally {
                neow3j.shutdown();
            }
        }
    }

}
