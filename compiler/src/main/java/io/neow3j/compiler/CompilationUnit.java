package io.neow3j.compiler;

public class CompilationUnit {

    public CompilationUnit(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private ClassLoader classLoader;

    private NeoModule neoModule;

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NeoModule getNeoModule() {
        return neoModule;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setNeoModule(NeoModule neoModule) {
        this.neoModule = neoModule;
    }
}
