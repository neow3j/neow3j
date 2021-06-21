package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.events.Event1Arg;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StaticVariablesTest {

    @Test
    public void test() throws IOException {
        CompilationUnit output =
                new Compiler().compile(StaticVariablesTestContract.class.getName());
        java.util.List<String> staticVars = output.getDebugInfo().getStaticVariables();
        assertThat(staticVars.get(0), is("ctx,InteropInterface,0"));
        assertThat(staticVars.get(1), is("owner,Hash160,1"));
        assertThat(staticVars.get(2), is("string,String,2"));
        assertThat(staticVars.get(3), is("byteString,ByteArray,3"));
        assertThat(staticVars.get(4), is("i,Integer,4"));
        assertThat(staticVars.get(5), is("byteArray,ByteArray,5"));
        assertThat(staticVars.get(6), is("map,Map,6"));
        assertThat(staticVars.get(7), is("obj,Any,7"));
        assertThat(staticVars.get(8), is("list,Array,8"));
    }

    public static class StaticVariablesTestContract {

        static StorageContext ctx = Storage.getStorageContext();
        static Hash160 owner = addressToScriptHash("NVq8SdaVYZ38QnGFV3ViDdFPubm21KG1An");
        static String string = "hello, world!";
        static ByteString byteString = StringLiteralHelper.hexToBytes("0102030405");
        static int i = 10;
        static byte[] byteArray = byteString.toByteArray();
        static Map<ByteString, ByteString> map = new Map<>();
        static Event1Arg<String> event;
        static MyClass obj = new MyClass(1);
        static List<String> list = new List<>(new String[]{"hello", "world"});

        public static void method() {
            event.fire("hello, world");
        }

        static class MyClass {

            public int i;

            public MyClass(int i) {
                this.i = i;
            }
        }

    }
}
