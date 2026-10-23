package io.neow3j.compiler;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

public class CompilerVersionTest {

    @Test
    public void compilerPropertiesIsExpanded() {
        String version = new Compiler().loadCompilerVersion();
        assertThat(version, matchesPattern("^[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?$"));
    }

}
