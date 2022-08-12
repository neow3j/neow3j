package io.neow3j.compiler;

import io.neow3j.devpack.Runtime;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class SwitchCaseIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            SwitchCaseIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void switchWithString() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("world")).getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(2));
        assertThat(retVal.get(1).getString(), is("isWorld"));

        r = ct.callInvokeFunction(testName, string("defaultCase")).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(4));
        assertThat(retVal.get(1).getString(), is("isDefault"));
    }

    @Test
    public void switchWithInt() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, integer(500)).getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(3));
        assertThat(retVal.get(1).getString(), is("isOtherCase"));

        r = ct.callInvokeFunction(testName, integer(-10)).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(4));
        assertThat(retVal.get(1).getString(), is("isDefault"));
    }

    @Test
    public void complexSwitch() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("hello"), integer(100))
                .getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(100));
        assertThat(retVal.get(1).getString(), containsString("isHello"));

        r = ct.callInvokeFunction(testName, string("hello"), integer(-10)).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(1000));
        assertThat(retVal.get(1).getString(), is("exceptionCaught"));
    }

    @Test
    public void switchWithStringTwoCases() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("world")).getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(2));
        assertThat(retVal.get(1).getString(), is("isWorld"));

        r = ct.callInvokeFunction(testName, string("defaultCase")).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(3));
        assertThat(retVal.get(1).getString(), is("isDefault"));
    }

    @Test
    public void switchWithStringOneCase() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("hello")).getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(1));
        assertThat(retVal.get(1).getString(), is("isHello"));

        r = ct.callInvokeFunction(testName, string("defaultCase")).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(2));
        assertThat(retVal.get(1).getString(), is("isDefault"));
    }

    @Test
    public void switchWithStringLocalVariableDeclaration() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("hello")).getInvocationResult();
        List<StackItem> retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(100));
        assertThat(retVal.get(1).getString(), is("isHello"));

        r = ct.callInvokeFunction(testName, string("defaultCase")).getInvocationResult();
        retVal = r.getStack().get(0).getList();
        assertThat(retVal.get(0).getInteger().intValue(), is(200));
        assertThat(retVal.get(1).getString(), is("isDefault"));
    }

    @Test
    public void largeSwitchCase() throws IOException {
        InvocationResult r = ct.callInvokeFunction(testName, string("hello")).getInvocationResult();
        assertThat(r.getStack().get(0).getInteger().intValue(), is(1));

        r = ct.callInvokeFunction(testName, string("concept")).getInvocationResult();
        assertThat(r.getStack().get(0).getInteger().intValue(), is(6));

        r = ct.callInvokeFunction(testName, string("giraffe")).getInvocationResult();
        assertThat(r.getStack().get(0).getInteger().intValue(), is(14));

        r = ct.callInvokeFunction(testName, string("unknown")).getInvocationResult();
        assertThat(r.getStack().get(0).getInteger().intValue(), is(15));
    }

    static class SwitchCaseIntegrationTestContract {

        public static Object[] switchWithString(String s) {
            int localInt = 0;
            String localString = "not set";
            switch (s) {
                case "hello":
                    localInt = 1;
                    localString = "isHello";
                    break;
                case "world":
                    localInt = 2;
                    localString = "isWorld";
                    break;
                case "otherCase":
                    localInt = 3;
                    localString = "isOtherCase";
                    break;
                default:
                    localInt = 4;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static Object[] switchWithInt(int i) {
            int localInt = 0;
            String localString = "not set";
            switch (i) {
                case 1:
                    localInt = 1;
                    localString = "isHello";
                    break;
                case 10:
                    localInt = 2;
                    localString = "isWorld";
                    break;
                case 500:
                    localInt = 3;
                    localString = "isOtherCase";
                    break;
                default:
                    localInt = 4;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static Object[] complexSwitch(String s, int i) throws Exception {
            int localInt = 0;
            String localString = "not set";
            switch (s) {
                case "hello":
                    if (i == 10) {
                        localInt = 1;
                        localString = "isHello";
                    } else {
                        try {
                            switch (i) {
                                case 20:
                                    localInt = 20;
                                    localString = "is" + "Hello" + Runtime.getPlatform();
                                    break;
                                case 100:
                                    localInt = 100;
                                    localString = "is" + "Hello" +
                                            Runtime.getCallingScriptHash().toByteString().toString();
                                    break;
                                default:
                                    throw new Exception();
                            }
                        } catch (Exception e) {
                            localInt = 1000;
                            localString = "exceptionCaught";
                        }
                    }
                    break;
                case "world":
                    localInt = 2;
                    localString = "isWorld";
                    break;
                case "otherCase":
                    localInt = 3;
                    localString = "isOtherCase";
                    break;
                case "yetAnotherCase":
                    localInt = i * 3 + 5;
                    localString = "is" + "Yet" + "Another" + "Case";
                    break;
                default:
                    localInt = 4;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static Object[] switchWithStringTwoCases(String s) {
            int localInt = 0;
            String localString = "not set";
            switch (s) {
                case "hello":
                    localInt = 1;
                    localString = "isHello";
                    break;
                case "world":
                    localInt = 2;
                    localString = "isWorld";
                    break;
                default:
                    localInt = 3;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static Object[] switchWithStringOneCase(String s) {
            int localInt = 0;
            String localString = "not set";
            switch (s) {
                case "hello":
                    localInt = 1;
                    localString = "isHello";
                    break;
                default:
                    localInt = 2;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static Object[] switchWithStringLocalVariableDeclaration(String s) {
            int localInt = 10;
            String localString = "not set";
            switch (s) {
                case "hello":
                    int i = 10;
                    i *= localInt;
                    localInt = i;
                    localString = "isHello";
                    break;
                default:
                    int j = 20;
                    j *= localInt;
                    localInt = j;
                    localString = "isDefault";
            }
            return new Object[]{localInt, localString};
        }

        public static int largeSwitchCase(String s) {
            int i;
            switch (s) {
                case "hello":
                    i = 1;
                    break;
                case "some":
                    i = 2;
                    break;
                case "other":
                    i = 3;
                    break;
                case "each":
                    i = 4;
                    break;
                case "structure":
                    i = 5;
                    break;
                case "concept":
                    i = 6;
                    break;
                case "abstract":
                    i = 7;
                    break;
                case "what":
                    i = 8;
                    break;
                case "not":
                    i = 9;
                    break;
                case "icarus":
                    i = 10;
                    break;
                case "flamingo":
                    i = 11;
                    break;
                case "pelican":
                    i = 12;
                    break;
                case "rhinoceros":
                    i = 13;
                    break;
                case "giraffe":
                    i = 14;
                    break;
                default:
                    i = 15;
            }
            return i;
        }

    }

}
