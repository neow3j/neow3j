package io.neow3j.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertionsTest {

    @Test
    public void testVerifyPrecondition() {
        Assertions.verifyPrecondition(true, "");
    }

    @Test
    public void testVerifyPreconditionFailure() {
        assertThrows(RuntimeException.class, () -> Assertions.verifyPrecondition(false, ""));
    }
}
