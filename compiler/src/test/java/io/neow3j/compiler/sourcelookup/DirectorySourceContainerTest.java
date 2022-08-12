package io.neow3j.compiler.sourcelookup;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.compiler.DebugInfo;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.annotations.Struct;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class DirectorySourceContainerTest {

    @Test
    public void createDebugInfoForContractThatCallsStaticMethodOfOtherClass() throws URISyntaxException, IOException {
        // The 'sources' directory is not the original java sources because when this test is ran the runtime will
        // not know the 'sources' location. Therefore, we mocked a `SourceLookupTest` class in the "resources" folder
        // under the "sources" directory.
        File sourcesDir = new File(this.getClass().getClassLoader().getResource("sources").toURI());
        ISourceContainer c = new DirectorySourceContainer(sourcesDir, true);
        CompilationUnit res = new Compiler().compile(Contract1.class.getName(), asList(c));
        DebugInfo dbg = res.getDebugInfo();
        // The source file is not directly in sources but one directory below in the folder 'inbetween'. I.e., the
        // SourceContainer has to recurse one level down.
        Path path = Paths.get("resources", "test", "sources", "inbetween", "io", "neow3j", "compiler", "sourcelookup",
                "DirectorySourceContainerTest.java");
        assertThat(dbg.getDocuments().get(0), containsString(path.toString()));

        // Check correct methods are referenced
        assertThat(dbg.getMethods().get(0).getName(), is(Contract1.class.getName() + ",method1"));
        assertThat(dbg.getMethods().get(1).getName(), is(Utility.class.getName() + ",method2"));

        // Check if the methods point to the right source file ("[0]").
        assertThat(dbg.getMethods().get(0).getSequencePoints().get(0), containsString("[0]"));
        assertThat(dbg.getMethods().get(1).getSequencePoints().get(0), containsString("[0]"));
    }

    @Test
    public void createDebugInfoForContractThatConstructsAndCallsAnObject() throws URISyntaxException, IOException {
        // The 'sources' directory is not the original java sources because when this test is ran the runtime will
        // not know the 'sources' location. Therefore, we mocked a `SourceLookupTest` class in the "resources" folder
        // under the "sources" directory.
        File sourcesDir = new File(this.getClass().getClassLoader().getResource("sources").toURI());
        ISourceContainer c = new DirectorySourceContainer(sourcesDir, true);
        CompilationUnit res = new Compiler().compile(Contract2.class.getName(), asList(c));
        DebugInfo dbg = res.getDebugInfo();
        // The source file is not directly in sources but one directory below in the folder 'inbetween'. I.e., the
        // SourceContainer has to recurse one level down.
        Path path = Paths.get("resources", "test", "sources", "inbetween", "io", "neow3j", "compiler", "sourcelookup",
                "DirectorySourceContainerTest.java");
        assertThat(dbg.getDocuments().get(0), containsString(path.toString()));

        // Check correct methods are referenced
        assertThat(dbg.getMethods().get(0).getName(), is(Contract2.class.getName() + ",createAndInvokeObject"));
        assertThat(dbg.getMethods().get(1).getName(), is(POJO.class.getName() + ",<init>"));
        assertThat(dbg.getMethods().get(2).getName(), is(POJO.class.getName() + ",getAtIndexMultiplied"));

        // Check if the methods point to the right source file ("[0]").
        assertThat(dbg.getMethods().get(0).getSequencePoints().get(0), containsString("[0]"));
        assertThat(dbg.getMethods().get(1).getSequencePoints().get(0), containsString("[0]"));
        assertThat(dbg.getMethods().get(2).getSequencePoints().get(0), containsString("[0]"));
    }

    @Test
    public void createDebugInfoForContractThatCallsMethodOfOtherClassInOtherFile() throws URISyntaxException,
            IOException {
        // The 'sources' directory is not the original java sources because when this test is ran the runtime will not
        // know the 'sources' location. Therefore, we mocked a `SourceLookupTest` class in the "resources" folder under
        // the "sources" directory.
        File sourcesDir = new File(this.getClass().getClassLoader().getResource("sources").toURI());
        ISourceContainer c = new DirectorySourceContainer(sourcesDir, true);
        CompilationUnit res = new Compiler().compile(SourceLookupTestContract3.class.getName(), asList(c));
        DebugInfo dbg = res.getDebugInfo();
        // The source file is not directly in sources but one directory below in the folder 'inbetween'. I.e., the
        // SourceContainer has to recurse one level down.
        Path path = Paths.get("resources", "test", "sources", "inbetween", "io", "neow3j", "compiler", "sourcelookup",
                "DirectorySourceContainerTest.java");
        assertThat(dbg.getDocuments().get(0), containsString(path.toString()));
        path = Paths.get("resources", "test", "sources", "io", "neow3j", "compiler", "sourcelookup",
                "TestSmartContract.java");
        assertThat(dbg.getDocuments().get(1), containsString(path.toString()));

        // Check correct methods are referenced
        assertThat(dbg.getMethods().get(0).getName(),
                is(SourceLookupTestContract3.class.getName() + ",getPlatformFromOtherClass"));
        assertThat(dbg.getMethods().get(1).getName(), is(TestSmartContract.class.getName() + ",getPlatform"));

        // Check if the methods point to the right source file ("[0]").
        assertThat(dbg.getMethods().get(0).getSequencePoints().get(0), containsString("[0]"));
        assertThat(dbg.getMethods().get(1).getSequencePoints().get(0), containsString("[1]"));
    }

    static class Contract1 {
        public static boolean method1() {
            return Utility.method2();
        }
    }

    static class Utility {
        public static boolean method2() {
            return true;
        }
    }

    static class Contract2 {
        public static int createAndInvokeObject() {
            POJO pojo = new POJO(2, new ByteString("hello, world"));
            return pojo.getAtIndexMultiplied(1);
        }
    }

    @Struct
    static class POJO {
        public int i;
        public ByteString s;

        public POJO(int i, ByteString s) {
            this.i = i;
            this.s = s;
        }

        public int getAtIndexMultiplied(int idx) {
            return s.get(idx) * i;
        }
    }

    static class SourceLookupTestContract3 {

        public static String getPlatformFromOtherClass() {
            return TestSmartContract.getPlatform();
        }
    }

}
