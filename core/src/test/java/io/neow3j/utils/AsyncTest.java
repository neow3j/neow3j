package io.neow3j.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsyncTest {

    @Test
    public void testRunSuccessful() throws Exception {
        assertThat(
                Async.run(() -> "anything").get(),
                is("anything")
        );
    }

    @Test
    public void testRunWithException() {
        assertThrows(ExecutionException.class,
                () -> Async.run(
                        () -> {
                            throw new RuntimeException("Exception");
                        }
                ).get()
        );
    }

}
