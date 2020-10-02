package io.neow3j.compiler;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.annotations.SupportedStandards;
import io.neow3j.devpack.neo.Account;
import java.io.IOException;
import org.junit.Test;

public class SupportedStandardsTest {

    @Test
    public void multiStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compileClass(
                MultiStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP5", "NEP10"));
    }

    @Test
    public void singleStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compileClass(
                SingleStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), hasSize(1));
        assertThat(res.getManifest().getSupportedStandards(), contains("NEP5"));
    }

}

@SupportedStandards({"NEP5", "NEP10"})
class MultiStandardContract {

    public static boolean main(byte[] scriptHash) {
        return Account.isStandard(scriptHash);
    }
}

@SupportedStandards("NEP5")
class SingleStandardContract {

    public static boolean main(byte[] scriptHash) {
        return Account.isStandard(scriptHash);
    }
}
