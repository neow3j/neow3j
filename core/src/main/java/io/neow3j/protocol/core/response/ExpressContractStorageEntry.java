package io.neow3j.protocol.core.response;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.prependHexPrefix;

/**
 * Neo-express uses a slightly different approach for communicated storage value encoding than the neo-modules
 * RPCServer plugin. Neo-express communicates with hexadecimal values (e.g., in the express-specific RPC
 * {@code expressgetstorage}), while the official RPCServer plugin uses Base64-encoded string values (e.g., in the RPC
 * {@code getstorage}).
 * <p>
 * This class provides overrides for the getter methods of {@code ContractStorageEntry}, so that the values are
 * returned in the expected encoding.
 */
public class ExpressContractStorageEntry extends ContractStorageEntry {

    /**
     * @return the key.
     */
    @Override
    public byte[] getKey() {
        return hexStringToByteArray(getKeyHex());
    }

    /**
     * @return the key as hexadecimal.
     */
    @Override
    public String getKeyHex() {
        return prependHexPrefix(key).toLowerCase();
    }

    /**
     * @return the value.
     */
    @Override
    public byte[] getValue() {
        return hexStringToByteArray(getValueHex());
    }

    /**
     * @return the key.
     */
    @Override
    public String getValueHex() {
        return prependHexPrefix(value).toLowerCase();
    }

}
