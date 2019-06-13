package io.neow3j.model.types;

public enum NodePluginType {

    APPLICATION_LOGS("ApplicationLogs"),
    CORE_METRICS("CoreMetrics"),
    IMPORT_BLOCKS("ImportBlocks"),
    RPC_NEP5_TRACKER("RpcNep5Tracker"),
    RPC_SECURITY("RpcSecurity"),
    RPC_SYSTEM_ASSET_TRACKER("RpcSystemAssetTrackerPlugin"),
    RPC_WALLET("RpcSystemAssetTrackerPlugin"),
    SIMPLE_POLICY("SimplePolicyPlugin"),
    STATES_DUMPER("StatesDumper");

    private String name;

    NodePluginType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static NodePluginType valueOfName(String name) {
        for (NodePluginType p : NodePluginType.values()) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException();
    }
}
