package io.neow3j.compiler;

import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.NeoVMStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.types.ContractParameter.integer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TryCatchBlocksTest {

    private static final String NEOVM_FAILED_ASSERT_MESSAGE = "ASSERT is executed with false result.";

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(TryCatchBlocks.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void hitExceptionInTryCatchBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryCatchBlock", integer(1));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
    }

    @Test
    public void dontHitExceptionInTryCatchBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryCatchBlock", integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertFalse(res.get(2).getBoolean());
    }

    @Test
    public void hitExceptionInTryFinallyBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryFinallyBlock", integer(1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void dontHitExceptionInTryFinallyBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryFinallyBlock", integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
    }

    @Test
    public void hitExceptionInTryCatchFinallyBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryCatchFinallyBlock", integer(1));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
    }

    @Test
    public void dontHitExceptionInTryCatchFinallyBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("tryCatchFinallyBlock", integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertFalse(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
    }

    @Test
    public void hitFirstExceptionInMultipleTryCatchFinallyBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("multipleTryCatchFinallyBlocks", integer(1), integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
        assertFalse(res.get(4).getBoolean());
        assertTrue(res.get(5).getBoolean());
        assertTrue(res.get(6).getBoolean());
    }

    @Test
    public void hitSecondExceptionInMultipleTryCatchFinallyBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("multipleTryCatchFinallyBlocks", integer(0), integer(1));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void dontHitAnyExceptionsInMultipleTryCatchFinallyBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("multipleTryCatchFinallyBlocks", integer(0), integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertFalse(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
        assertFalse(res.get(4).getBoolean());
        assertTrue(res.get(5).getBoolean());
        assertTrue(res.get(6).getBoolean());
    }

    @Test
    public void hitFirstExceptionInNestedTryCatchFinallyBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("nestedTryCatchFinallyBlocks", integer(1), integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertFalse(res.get(2).getBoolean());
        assertFalse(res.get(3).getBoolean());
        assertFalse(res.get(4).getBoolean());
        assertFalse(res.get(5).getBoolean());
        assertFalse(res.get(6).getBoolean());
        assertTrue(res.get(7).getBoolean());
        assertTrue(res.get(8).getBoolean());
    }

    @Test
    public void hitSecondExceptionInNestedTryCatchBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("nestedTryCatchFinallyBlocks", integer(0), integer(1));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
        assertFalse(res.get(3).getBoolean());
        assertTrue(res.get(4).getBoolean());
        assertTrue(res.get(5).getBoolean());
        assertTrue(res.get(6).getBoolean());
        assertFalse(res.get(7).getBoolean());
        assertTrue(res.get(8).getBoolean());
    }

    @Test
    public void hitNoExceptionsInNestedTryCatchBlocks() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("nestedTryCatchFinallyBlocks", integer(0), integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertFalse(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
        assertFalse(res.get(4).getBoolean());
        assertTrue(res.get(5).getBoolean());
        assertTrue(res.get(6).getBoolean());
        assertFalse(res.get(7).getBoolean());
        assertTrue(res.get(8).getBoolean());
    }

    @Test
    public void hitExceptionsInMethodThrowingException() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("catchExceptionFromMethod", integer(1));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
    }

    @Test
    public void dontHitExceptionsInMethodThrowingException() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("catchExceptionFromMethod", integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
    }

    @Test
    public void hitExceptionInNestedBlockInCatch() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("nestedBlockInCatch", integer(1), integer(1));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(res.get(0).getBoolean());
        assertTrue(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
        assertTrue(res.get(4).getBoolean());
    }

    @Test
    public void dontHitExceptionsInNestedBlockInCatch() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("nestedBlockInCatch", integer(1), integer(0));
        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(res.get(0).getBoolean());
        assertFalse(res.get(1).getBoolean());
        assertTrue(res.get(2).getBoolean());
        assertTrue(res.get(3).getBoolean());
        assertTrue(res.get(4).getBoolean());
    }

    @Test
    public void innerVarAssignmentAndEmptyCatch() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem res = response.getInvocationResult().getStack().get(0);
        assertTrue(res.getBoolean());
    }

    @Test
    public void innerInitVarAndEmptyCatch() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem res = response.getInvocationResult().getStack().get(0);
        assertTrue(res.getBoolean());
    }

    @Test
    public void emptyTryCatch() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem res = response.getInvocationResult().getStack().get(0);
        assertTrue(res.getBoolean());
    }

    @Test
    public void getCaughtExceptionMessage() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        StackItem res = response.getInvocationResult().getStack().get(0);
        assertThat(res.getString(), is("Not allowed."));
    }

    @Test
    public void tryToCatchAssertion() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(11));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
        assertThat(response.getInvocationResult().getException(), is(NEOVM_FAILED_ASSERT_MESSAGE));

        response = ct.callInvokeFunction(testName, integer(12));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("neoww"));
    }

    static class TryCatchBlocks {

        public static boolean[] tryCatchBlock(int i) {
            boolean[] b = new boolean[3];
            try {
                if (i == 1) {
                    b[0] = true;
                    throw new Exception("Exception");
                }
                b[1] = true;
            } catch (Exception e) {
                b[2] = true;
            }
            return b;
        }

        public static boolean[] tryFinallyBlock(int i) throws Exception {
            boolean[] b = new boolean[3];
            try {
                if (i == 1) {
                    b[0] = true;
                    throw new Exception("Exception");
                }
                b[1] = true;
            } finally {
                b[2] = true;
            }
            return b;
        }

        public static boolean[] tryCatchFinallyBlock(int i) {
            boolean[] b = new boolean[4];
            try {
                if (i == 1) {
                    b[0] = true;
                    throw new Exception("Exception");
                }
                b[1] = true;
            } catch (Exception e) {
                b[2] = true;
            } finally {
                b[3] = true;
            }
            return b;
        }

        public static boolean[] multipleTryCatchFinallyBlocks(int i, int j) throws Exception {
            boolean[] b = new boolean[7];
            try {
                if (i == 1) {
                    b[0] = true;
                    throw new Exception("Exception");
                }
                b[1] = true;
            } catch (Exception e) {
                b[2] = true;
            } finally {
                b[3] = true;
            }

            try {
                if (j == 1) {
                    b[4] = true;
                    throw new Exception("Exception");
                }
                b[5] = true;
            } finally {
                b[6] = true;
            }
            return b;
        }

        public static boolean[] nestedTryCatchFinallyBlocks(int i, int j) {
            boolean[] b = new boolean[9];
            try {
                if (i == 1) {
                    b[0] = true;
                    throw new Exception("1. Exception");
                }
                b[1] = true;
                try {
                    if (j == 1) {
                        b[2] = true;
                        throw new Exception("2. Exception");
                    }
                    b[3] = true;
                } catch (Exception e) {
                    b[4] = true;
                } finally {
                    b[5] = true;
                }
                b[6] = true;
            } catch (Exception e) {
                b[7] = true;
            } finally {
                b[8] = true;
            }
            return b;
        }

        public static boolean[] catchExceptionFromMethod(int i) {
            boolean[] b = new boolean[3];
            try {
                throwException(i);
                b[0] = true;
            } catch (Exception e) {
                b[1] = true;
            } finally {
                b[2] = true;
            }
            return b;
        }

        private static void throwException(int i) throws Exception {
            if (i == 1) {
                throw new Exception("Exception");
            }
        }

        public static boolean[] nestedBlockInCatch(int i, int j) {
            boolean[] b = new boolean[5];
            try {
                if (i == 1) {
                    throw new Exception("Exception");
                }
            } catch (Exception e1) {
                try {
                    if (j == 1) {
                        throw new Exception("2. Exception");
                    }
                    b[0] = true;
                } catch (Exception e2) {
                    b[1] = true;
                } finally {
                    b[2] = true;
                }
                b[3] = true;
            } finally {
                b[4] = true;
            }
            return b;
        }

        public static boolean innerVarAssignmentAndEmptyCatch() {
            String test;
            try {
                test = "io/neow3j/test";
            } catch (Exception e) {

            }
            return true;
        }

        public static boolean innerInitVarAndEmptyCatch() {
            try {
                String test = "io/neow3j/test";
            } catch (Exception e) {

            }
            return true;
        }

        public static boolean emptyTryCatch() {
            try {

            } catch (Exception e) {

            }
            return true;
        }

        public static String getCaughtExceptionMessage() {
            try {
                throw new Exception("Not allowed.");
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        public static String tryToCatchAssertion(int i) {
            try {
                assert i == 12;
            } catch (Exception e) {
                return e.getMessage();
            }
            return "neoww";
        }

    }

}
