package io.neow3j.devpack.constants;

/**
 * Standards for smart contracts.
 */
public enum NeoStandard {
    NEP_11("NEP-11"),
    NEP_17("NEP-17"),
    // This value is required as default dummy value in the SupportedStandards annotation interface.
    None("");

    private final String standard;

    NeoStandard(String standard) {
        this.standard = standard;
    }

    public String getStandard() {
        return standard;
    }

}
