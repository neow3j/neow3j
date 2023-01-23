package io.neow3j.neofs.lib;

public class NeoFSLib {

    private final NeoFSLibInterface nativeLib;

    public NeoFSLib() throws Exception {
        System.out.println("\n------------------------");
        System.out.println("# Loading Native Library");
        System.out.println("------------------------");
        nativeLib = NeoFSLibLoader.load();
        System.out.println("-----------------------");
        System.out.println("# Native Library Loaded");
        System.out.println("-----------------------\n");
    }

    public NeoFSLibInterface getNativeLib() {
        return nativeLib;
    }

}
