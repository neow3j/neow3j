package io.neow3j.devpack.framework;

/**
 * The key-value storage for a specific prefix in a smart contract's storage.
 */
public class StorageMap {

    private StorageContext context;
    private byte[] prefix;

    public StorageMap(StorageContext context, byte[] prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    public StorageMap(StorageContext context, byte[] prefix, int i) {
        this.context = context;
        this.prefix = prefix;
    }

    public void delete(byte[] key) {
        byte[] k = SmartContract.concat(this.prefix, key);
        Storage.delete(this.context, k);
    }

//    public void delete(String key) {
//        byte[] k = SmartContract.concat(this.prefix, SmartContract.toByteArray(key));
//        Storage.delete(this.context, k);
//    }

//    public static byte[] Get(this StorageMap map, byte[] key)
//    {
//        byte[] k = map.Prefix.Concat(key);
//        return Storage.Get(map.Context, k);
//    }
//
//    public static byte[] Get(this StorageMap map, string key)
//    {
//        byte[] k = map.Prefix.Concat(key.ToByteArray());
//        return Storage.Get(map.Context, k);
//    }
//
//    public static void Put(this StorageMap map, byte[] key, byte[] value)
//    {
//        byte[] k = map.Prefix.Concat(key);
//        Storage.Put(map.Context, k, value);
//    }
//
//    public static void Put(this StorageMap map, byte[] key, BigInteger value)
//    {
//        byte[] k = map.Prefix.Concat(key);
//        Storage.Put(map.Context, k, value);
//    }
//
//    public static void Put(this StorageMap map, byte[] key, string value)
//    {
//        byte[] k = map.Prefix.Concat(key);
//        Storage.Put(map.Context, k, value);
//    }
//
//    public static void Put(this StorageMap map, string key, byte[] value)
//    {
//        byte[] k = map.Prefix.Concat(key.ToByteArray());
//        Storage.Put(map.Context, k, value);
//    }
//
//    public static void Put(this StorageMap map, string key, BigInteger value)
//    {
//        byte[] k = map.Prefix.Concat(key.ToByteArray());
//        Storage.Put(map.Context, k, value);
//    }
//
//    public static void Put(this StorageMap map, string key, string value)
//    {
//        byte[] k = map.Prefix.Concat(key.ToByteArray());
//        Storage.Put(map.Context, k, value);
//    }
}
