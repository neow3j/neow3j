package io.neow3j.utils;

import static io.neow3j.utils.ClassUtils.getClassName;
import static io.neow3j.utils.ClassUtils.internalNameToFullyQualifiedName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

public class ClassUtilsTest {

    @Test
    public void testGetClassNameHappyPath() {
        String simpleClassName = getClassName("io.neow3j.blah.Test");
        assertThat(simpleClassName, is("Test"));
    }

    @Test
    public void testGetClassNameHappyPathWithSlashes() {
        String fqn = internalNameToFullyQualifiedName("io/neow3j/blah/Test");
        String simpleClassName = getClassName(fqn);
        assertThat(simpleClassName, is("Test"));
    }

    @Test
    public void testGetClassNameNoClass() {
        String simpleClassName = getClassName("io.neow3j.blah.");
        assertThat(simpleClassName, is(nullValue()));
    }

    @Test
    public void testGetClassNameEmpty() {
        String simpleClassName = getClassName("");
        assertThat(simpleClassName, is(nullValue()));
    }

    @Test
    public void testInternalNameToFQNHappyPath() {
        String fqn = internalNameToFullyQualifiedName("io/neow3j/blah/Test");
        assertThat(fqn, is("io.neow3j.blah.Test"));
    }

    @Test
    public void testInternalNameToFQNEmpty() {
        String fqn = internalNameToFullyQualifiedName("");
        assertThat(fqn, is(""));
    }

}
