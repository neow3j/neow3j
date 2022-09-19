package io.neow3j.neofs.lib;

public class NeoFSLib {

    private final NeoFSLibInterface nativeLib;

    public NeoFSLib() throws Exception {
        System.out.println("\n# Loading native library");
        nativeLib = NeoFSLibLoader.load();
        System.out.println("# Native library loaded\n");
    }

    public NeoFSLibInterface getNativeLib() {
        return nativeLib;
    }

}
