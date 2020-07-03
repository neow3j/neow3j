package io.neow3j.devpack.compiler;

import java.util.HashMap;
import java.util.Map;

public class NeoModule {

    Map<String, NeoMethod> methods = new HashMap<>();

    void addMethod(NeoMethod method) {
        methods.put(method.id, method);
    }

}
