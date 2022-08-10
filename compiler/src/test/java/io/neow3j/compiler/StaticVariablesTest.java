package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.events.Event1Arg;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class StaticVariablesTest {

    @Test
    public void test() throws IOException {
        CompilationUnit output =
                new Compiler().compile(StaticVariablesTestContract.class.getName());
        java.util.List<String> staticVars = output.getDebugInfo().getStaticVariables();
        assertThat(staticVars, containsInAnyOrder(
                "ctx,InteropInterface,0",
                "owner,Hash160,1",
                "string,String,2",
                "byteString,ByteArray,3",
                "i,Integer,4",
                "byteArray,ByteArray,5",
                "map,Map,6",
                "obj,Any,7",
                "list,Array,8"));
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

        @Struct
        static class MyClass {
            public int i;

            public MyClass(int i) {
                this.i = i;
            }
        }
    }

}
