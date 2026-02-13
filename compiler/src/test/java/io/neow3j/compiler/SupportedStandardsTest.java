package io.neow3j.compiler;

import io.neow3j.devpack.annotations.SupportedStandard;
import io.neow3j.devpack.annotations.SupportedStandard.SupportedStandards;
import io.neow3j.devpack.constants.NeoStandard;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that the provided supported standards from {@link NeoStandard} are set correctly in the manifest of the
 * compiled contract when using the {@link SupportedStandard} annotation.
 * <p>>
 * Neow3j does not enforce that the contract actually implements the declared standards, it only checks that the
 * annotation is used correctly and that the provided standard values are valid. The actual implementation of the
 * standards is not checked by the compiler.
 */
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
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP-11", "custom-standard-value"));
    }

    @Test
    public void multiStandardsContract() throws IOException {
        CompilationUnit res = new Compiler().compile(MultiStandardsContract.class.getName());
        assertThat(res.getManifest().getSupportedStandards(), containsInAnyOrder("NEP-17", "custom-standard"));
    }

    @Test
    public void allStandardContract() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(AllStandardContract.class.getName());
        List<String> supportedStandards = compUnit.getManifest().getSupportedStandards();
        System.out.println(supportedStandards);
        assertThat(supportedStandards,
                containsInAnyOrder(
                        "NEP-11",
                        "NEP-17",
                        "NEP-24",
                        "NEP-26",
                        "NEP-27"
                )
        );
    }

    @Test
    public void IllegalUsageOfAnnotation() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(InvalidUsageOfSupportedStandardAnnotation.class.getName()));
        assertThat(thrown.getMessage(),
                is("A @SupportedStandard annotation must only have one of the attributes 'neoStandard' or " +
                        "'customStandard' set."));
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

    @SupportedStandard(neoStandard = NeoStandard.NEP_11)
    @SupportedStandard(neoStandard = NeoStandard.NEP_17)
    @SupportedStandard(neoStandard = NeoStandard.NEP_24)
    @SupportedStandard(neoStandard = NeoStandard.NEP_26)
    @SupportedStandard(neoStandard = NeoStandard.NEP_27)
    static class AllStandardContract {

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
