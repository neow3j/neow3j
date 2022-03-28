package io.neow3j.compiler;

import io.neow3j.devpack.annotations.SupportedStandard;
import io.neow3j.devpack.annotations.SupportedStandard.SupportedStandards;
import io.neow3j.devpack.constants.NeoStandard;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

public class SupportedStandardsTest {

    @Test
    public void singleStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compile(SingleStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), hasSize(1));
        assertThat(res.getManifest().getSupportedStandards(), contains("NEP-17"));
    }

    @Test
    public void multiSingleStandardContract() throws IOException {
        CompilationUnit res = new Compiler().compile(MultiSingleStandardContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP-11",
                "custom-standard-value"));
    }

    @Test
    public void multiStandardsContract() throws IOException {
        CompilationUnit res = new Compiler().compile(MultiStandardsContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP-17",
                "custom-standard"));
    }

    @Test
    public void IllegalUsageOfAnnotation() {
        assertThrows("A @SupportedStandard annotation must only have one of the attributes " +
                        "'neoStandard' or 'customStandard' set.",
                CompilerException.class,
                () -> new Compiler()
                        .compile(InvalidUsageOfSupportedStandardAnnotation.class.getName()));
    }

    @SupportedStandard(neoStandard = NeoStandard.NEP_17)
    static class SingleStandardContract {

        public static boolean method() {
            return true;
        }
    }

    @SupportedStandard(neoStandard = NeoStandard.NEP_11)
    @SupportedStandard(customStandard = "custom-standard-value")
    static class MultiSingleStandardContract {

        public static boolean method() {
            return true;
        }
    }

    @SupportedStandards({
            @SupportedStandard(neoStandard = NeoStandard.NEP_17),
            @SupportedStandard(customStandard = "custom-standard")}
    )
    static class MultiStandardsContract {

        public static boolean method() {
            return true;
        }
    }

    @SupportedStandard(neoStandard = NeoStandard.NEP_17, customStandard = "custumStandard")
    static class InvalidUsageOfSupportedStandardAnnotation {

        public static boolean method() {
            return true;
        }
    }

}
