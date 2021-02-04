package io.neow3j.compiler;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.NefFile.MethodToken;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.model.types.CallFlags;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class MethodTokensTest {

    @Test
    public void multipleMethodTokensShouldShowUpInTheNefFileButWithoutDuplicates()
            throws IOException {
        CompilationUnit compUnit = new Compiler().compileClass(
                MultipleMethodTokensContract.class.getName());
        List<MethodToken> tokens = compUnit.getNefFile().getMethodTokens();
        assertThat(tokens, hasSize(4));

        assertThat(tokens.get(0).getHash(), is(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        assertThat(tokens.get(0).getParametersCount(), is(1));
        assertTrue(tokens.get(0).hasReturnValue());
        assertThat(tokens.get(0).getMethod(), is("balanceOf"));
        assertThat(tokens.get(0).getCallFlags(), is(CallFlags.ALL));

        assertThat(tokens.get(1).getHash(), is(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        assertThat(tokens.get(1).getMethod(), is("decimals"));

        assertThat(tokens.get(2).getHash(), is(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        assertThat(tokens.get(2).getMethod(), is("getGasPerBlock"));

        assertThat(tokens.get(3).getHash(), is(io.neow3j.contract.GasToken.SCRIPT_HASH));
        assertThat(tokens.get(3).getMethod(), is("decimals"));
    }

    @Test
    public void singleMethodTokenShouldShowUpInTheNefFileButWithoutDuplicates()
            throws IOException {
        CompilationUnit compUnit = new Compiler().compileClass(
                SingleMethodTokenContract.class.getName());
        List<MethodToken> tokens = compUnit.getNefFile().getMethodTokens();
        assertThat(tokens, hasSize(1));

        assertThat(tokens.get(0).getHash(), is(io.neow3j.contract.NeoToken.SCRIPT_HASH));
        assertThat(tokens.get(0).getParametersCount(), is(0));
        assertTrue(tokens.get(0).hasReturnValue());
        assertThat(tokens.get(0).getMethod(), is("decimals"));
        assertThat(tokens.get(0).getCallFlags(), is(CallFlags.ALL));
    }

    static class MultipleMethodTokensContract {

        public static int callNeoTokenMethods() {
            int g = NeoToken.balanceOf(StringLiteralHelper.addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
            return NeoToken.decimals();
        }

        public static int callNeoTokenMethodsAgain() {
            int g = NeoToken.getGasPerBlock();
            return NeoToken.decimals();
        }

        public static int callGasTokenMethod() {
            return GasToken.decimals();
        }
    }

    static class SingleMethodTokenContract {

        public static int callNeoTokenMethods() {
            return NeoToken.decimals();
        }

        public static int callNeoTokenMethodAgain() {
            return NeoToken.decimals();
        }
    }

}
