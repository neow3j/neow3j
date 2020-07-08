package io.neow3j.devpack.compiler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.tree.ClassNode;

public class NeoModule {

    Map<String, NeoMethod> methods = new HashMap<>();
    ClassNode asmClass;

    public NeoModule(ClassNode asmClass) {
        this.asmClass = asmClass;
    }

    void addMethod(NeoMethod method) {
        methods.put(method.id, method);
    }

    int byteSize() {
        return methods.values().stream().map(NeoMethod::byteSize).reduce(Integer::sum).get();
    }

    byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(byteSize());
        methods.values().forEach(m -> b.put(m.toByteArray()));
        return b.array();
    }

}
