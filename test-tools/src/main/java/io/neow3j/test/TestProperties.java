package io.neow3j.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class TestProperties {

    private static TestProperties instance = null;
    private final Properties properties;

    protected TestProperties() throws IOException, URISyntaxException {
        properties = new Properties();
        properties.load(getClass().getResourceAsStream("/test.properties"));
    }

    public static TestProperties getInstance() {
        if (instance == null) {
            try {
                instance = new TestProperties();
            } catch (IOException | URISyntaxException ioe) {
                ioe.printStackTrace();
            }
        }
        return instance;
    }

    public static String defaultAccountAddress() {
        return getValue("defaultAccountAddress");
    }

    public static String defaultAccountWIF() {
        return getValue("defaultAccountWIF");
    }

    public static String defaultAccountPublicKey() {
        return getValue("defaultAccountPublicKey");
    }

    public static String defaultAccountPrivateKey() {
        return getValue("defaultAccountPrivateKey");
    }

    public static String defaultAccountEncryptedPrivateKey() {
        return getValue("defaultAccountEncryptedPrivateKey");
    }

    public static String defaultAccountPassword() {
        return getValue("defaultAccountPassword");
    }

    public static String defaultAccountScriptHash() {
        return getValue("defaultAccountScriptHash");
    }

    public static String defaultAccountVerificationScript() {
        return getValue("defaultAccountVerificationScript");
    }

    public static String client1AccountWIF() {
        return getValue("client1AccountWIF");
    }

    public static String client2AccountWIF() {
        return getValue("client2AccountWIF");
    }

    public static String neo3PrivateNetContainerImg() {
        return getValue("neo3PrivateNetContainerImg");
    }

    public static String neoExpressDockerImage() {
        return getValue("neoExpressDockerImage");
    }

    public static String committeeAccountAddress() {
        return getValue("committeeAccountAddress");
    }

    public static String committeeAccountScriptHash() {
        return getValue("committeeAccountScriptHash");
    }

    public static String committeeAccountVerificationScript() {
        return getValue("committeeAccountVerificationScript");
    }

    public static String neoTokenHash() {
        return getValue("neoTokenHash");
    }

    public static String gasTokenHash() {
        return getValue("gasTokenHash");
    }

    public static String contractManagementHash() {
        return getValue("contractManagementHash");
    }

    public static String roleManagementHash() {
        return getValue("roleManagementHash");
    }

    public static String stdLibHash() {
        return getValue("stdLibHash");
    }

    public static String cryptoLibHash() {
        return getValue("cryptoLibHash");
    }

    public static String ledgerContractHash() {
        return getValue("ledgerContractHash");
    }

    public static String policyContractHash() {
        return getValue("policyContractHash");
    }

    public static String oracleContractHash() {
        return getValue("oracleContractHash");
    }

    public static String gasTokenName() {
        return getValue("gasTokenName");
    }

    public static String getValue(String key) {
        return getInstance().properties.getProperty(key);
    }

}
