package io.neow3j.utils;

import static io.neow3j.utils.ClassUtils.getClassInputStreamForClassName;
import static io.neow3j.utils.ClassUtils.getClassName;
import static io.neow3j.utils.ClassUtils.getClassNameForInternalName;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.io.InputStream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class ClassUtilsTest {

    @Test
    public void testGetClassNameHappyPath() {
        String simpleClassName = getClassName("io.neow3j.blah.Test");
        assertThat(simpleClassName, is("Test"));
    }

    @Test
    public void testGetClassNameHappyPathWithSlashes() {
        String fqn = getFullyQualifiedNameForInternalName("io/neow3j/blah/Test");
        String simpleClassName = getClassName(fqn);
        assertThat(simpleClassName, is("Test"));
    }

    @Test
    public void testGetClassNameNoClass() {
        String simpleClassName = getClassName("io.neow3j.blah.");
        assertThat(simpleClassName, is(""));
    }

    @Test
    public void testGetClassNameEmpty() {
        String simpleClassName = getClassName("");
        assertThat(simpleClassName, is(""));
    }

    @Test
    public void testInternalNameToFQNHappyPath() {
        String fqn = getFullyQualifiedNameForInternalName("io/neow3j/blah/Test");
        assertThat(fqn, is("io.neow3j.blah.Test"));
    }

    @Test
    public void testInternalNameToFQNEmpty() {
        String fqn = getFullyQualifiedNameForInternalName("");
        assertThat(fqn, is(""));
    }

    @Test
    public void getClassNameFromInternalNameReturnsCorrectNameForProperInternalName() {
        String result = getClassNameForInternalName("io/neow3j/blah/Test");
        assertThat(result, is("Test"));
    }

    @Test
    public void getClassNameFromInternalNameReturnsEmptyStringForEmptyInternalName() {
        String result = getClassNameForInternalName("");
        assertThat(result, is(""));
    }

    @Test
    public void testGetClassInputStreamForClassName() throws IOException {
        InputStream result = getClassInputStreamForClassName("io.neow3j.utils.ClassUtilsTest",
                this.getClass().getClassLoader());
        assertThat(result, notNullValue());
        assertThat(result.available(), Matchers.greaterThan(0));
    }
}
