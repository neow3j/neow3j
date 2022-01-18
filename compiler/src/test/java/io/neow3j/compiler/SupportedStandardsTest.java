package io.neow3j.compiler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import io.neow3j.devpack.annotations.SupportedStandards;

import java.io.IOException;
import org.junit.Test;

public class SupportedStandardsTest {

    @Test
    public void multiStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compile(
                MultiStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP17", "NEP10"));
    }

    @Test
    public void singleStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compile(
                SingleStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), hasSize(1));
        assertThat(res.getManifest().getSupportedStandards(), contains("NEP17"));
    }

    @SupportedStandards({"NEP17", "NEP10"})
    static class MultiStandardContract {

        public static boolean method() {
            return true;
        }
    }

    @SupportedStandards("NEP17")
    static class SingleStandardContract {

        public static boolean method() {
            return true;
        }
    }

}
