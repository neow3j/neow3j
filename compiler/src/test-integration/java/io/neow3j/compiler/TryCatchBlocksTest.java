package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class TryCatchBlocksTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(TryCatchBlocks.class.getName());
    }

    @Test
    public void hitExceptionInTryCatchBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryCatchBlock", integer(1));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(1).asAny().getValue(), nullValue());
        assertThat(res.get(2).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void dontHitExceptionInTryCatchBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryCatchBlock", integer(0));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asAny().getValue(), nullValue());
        assertThat(res.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(2).asAny().getValue(), nullValue());
    }

    @Test
    public void hitExceptionInTryFinallyBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryFinallyBlock", integer(1));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void dontHitExceptionInTryFinallyBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryFinallyBlock", integer(0));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asAny().getValue(), nullValue());
        assertThat(res.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(2).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void hitExceptionInTryCatchFinallyBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryCatchFinallyBlock", integer(1));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(1).asAny().getValue(), nullValue());
        assertThat(res.get(2).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(3).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void dontHitExceptionInTryCatchFinallyBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryCatchFinallyBlock", integer(0));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asAny().getValue(), nullValue());
        assertThat(res.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(2).asAny().getValue(), nullValue());
        assertThat(res.get(3).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void hitFirstExceptionInNestedTryCatchFinallyBlocks() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("tryCatchFinallyBlocks", integer(1),
                integer(0));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(1).asAny().getValue(), nullValue());
        assertThat(res.get(2).asAny().getValue(), nullValue());
        assertThat(res.get(3).asAny().getValue(), nullValue());
        assertThat(res.get(4).asAny().getValue(), nullValue());
        assertThat(res.get(5).asAny().getValue(), nullValue());
        assertThat(res.get(6).asAny().getValue(), nullValue());
        assertThat(res.get(7).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(8).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void hitSecondExceptionInNestedTryCatchBlocks() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("nestedTryCatchFinallyBlocks", integer(0),
                integer(1));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asAny().getValue(), nullValue());
        assertThat(res.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(2).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(3).asAny().getValue(), nullValue());
        assertThat(res.get(4).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(5).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(6).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(7).asAny().getValue(), nullValue());
        assertThat(res.get(8).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void hitNoExceptionsInNestedTryCatchBlocks() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("nestedTryCatchFinallyBlocks", integer(0),
                integer(0));
        ArrayStackItem res = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(res.get(0).asAny().getValue(), nullValue());
        assertThat(res.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(2).asAny().getValue(), nullValue());
        assertThat(res.get(3).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(4).asAny().getValue(), nullValue());
        assertThat(res.get(5).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(6).asInteger().getValue().intValue(), is(1));
        assertThat(res.get(7).asAny().getValue(), nullValue());
        assertThat(res.get(8).asInteger().getValue().intValue(), is(1));
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

    }
}
