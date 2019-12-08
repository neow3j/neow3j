package io.neow3j.utils;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncTest {

    @Test
    public void testRunSuccessful() throws Exception {
        assertThat(
                Async.run(() -> "anything").get(),
                is("anything")
        );
    }

    @Test(expected = ExecutionException.class)
    public void testRunWithException() throws Exception {
        Async.run(() -> {
            throw new RuntimeException("Exception");
        }).get();
    }

}
