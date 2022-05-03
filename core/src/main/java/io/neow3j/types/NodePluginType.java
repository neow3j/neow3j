package io.neow3j.types;

public enum NodePluginType {

    APPLICATION_LOGS("ApplicationLogs"),
    CORE_METRICS("CoreMetrics"),
    IMPORT_BLOCKS("ImportBlocks"),
    LEVEL_DB_STORE("LevelDBStore"),
    ROCKS_DB_STORE("RocksDBStore"),
    RPC_NEP17_TRACKER("RpcNep17Tracker"),
    RPC_SECURITY("RpcSecurity"),
    RPC_SERVER_PLUGIN("RpcServerPlugin"),
    RPC_SYSTEM_ASSET_TRACKER("RpcSystemAssetTrackerPlugin"),
    RPC_WALLET("RpcSystemAssetTrackerPlugin"),
    SIMPLE_POLICY("SimplePolicyPlugin"),
    STATES_DUMPER("StatesDumper"),
    SYSTEM_LOG("SystemLog");

    private final String name;

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
