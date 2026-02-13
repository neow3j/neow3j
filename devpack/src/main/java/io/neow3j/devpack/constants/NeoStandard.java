package io.neow3j.devpack.constants;

/**
 * Enumerates supported Neo standards that can be declared in a smart contract's
 * {@link io.neow3j.devpack.annotations.SupportedStandard @SupportedStandard} annotation.
 * <p>
 * Declaring supported standards enables interoperability by allowing other contracts, tools, and applications to
 * identify and interact with a contract through well-defined interfaces.
 * <p>
 * For details on the individual standards, see the
 * <a href="https://github.com/neo-project/proposals/">NEO Enhancement Proposals (NEPs)</a>.
 */
public enum NeoStandard {
    /**
     * Represents the NEP-11 non-fungible token (NFT) standard in the NEO Smart Economy.
     * <p>
     * NEP-11 defines a standardized model for non-fungible tokens, enabling the tracking, exchange, and
     * enforcement of ownership for unique digital assets. Each token instance is distinct and may contain immutable
     * metadata (e.g., descriptive attributes), while ownership information remains transferable.
     * <p>
     * The standard also supports optional divisibility within a single NFT, allowing high-value assets to be
     * fractionally represented when required.
     *
     * @see <a href="https://github.com/neo-project/proposals/blob/master/nep-11.mediawiki">NEP-11 Specification</a>
     */
    NEP_11("NEP-11"),
    /**
     * Represents the NEP-17 fungible token standard for the NEO blockchain.
     * <p>
     * NEP-17 defines a generalized interaction mechanism for tokenized Smart Contracts, enabling consistent
     * deployment and invocation patterns across the ecosystem. By standardizing core token operations (e.g., balance
     * queries and transfers), it eliminates the need for systems to maintain contract-specific APIs for functionally
     * similar token contracts.
     * <p>
     * This standard improves interoperability, reduces integration complexity, and establishes a uniform
     * interface for fungible tokens within the NEO Smart Economy.
     *
     * @see <a href="https://github.com/neo-project/proposals/blob/master/nep-17.mediawiki">NEP-17 Specification</a>
     */
    NEP_17("NEP-17"),
    /**
     * Represents the NEP-24 royalty standard for non-fungible tokens (NFTs) in the NEO Smart Economy.
     * <p>
     * NEP-24 defines a global standard for retrieving royalty payment information associated with NEP-11 NFTs. It
     * enables NFT marketplaces and other integrators to query royalty details in a consistent and interoperable
     * manner, facilitating automated royalty payments across the ecosystem.
     * <p>
     * By standardizing royalty declaration and discovery, NEP-24 supports creator compensation, reduces the need for
     * marketplace-specific royalty agreements, and enhances copyright protection. The standard is designed to be
     * compatible with NEP-11.
     *
     * @see <a href="https://github.com/neo-project/proposals/blob/master/nep-24.mediawiki">NEP-24 Specification</a>
     */
    NEP_24("NEP-24"),
    /**
     * Represents the NEP-26 receiver callback standard for NEP-11 token transfers on the NEO blockchain.
     * <p>
     * NEP-26 specifies the callback method that Smart Contracts must implement in order to properly receive
     * transfers of NEP-11 non-fungible tokens. It formalizes the receiver-side processing logic, clarifying behavior
     * that was previously only implicitly defined in NEP-11.
     * <p>
     * This standard enables contracts to explicitly declare support for handling incoming NEP-11 transfers. Typical
     * use cases include staking, custodial deposits, escrow mechanisms, and token exchange or minting flows where a
     * contract accepts an NFT and returns another asset.
     *
     * @see <a href="https://github.com/neo-project/proposals/blob/master/nep-26.mediawiki">NEP-26 Specification</a>
     */
    NEP_26("NEP-26"),
    /**
     * Represents the NEP-27 receiver callback standard for NEP-17 token transfers on the NEO blockchain.
     * <p>
     * NEP-27 specifies the callback method that Smart Contracts must implement in order to properly receive
     * transfers of NEP-17 fungible tokens. It formalizes the receiver-side processing logic and clarifies behavior
     * that was only implicitly defined in the NEP-17 token standard.
     * <p>
     * This standard allows contracts to explicitly declare support for handling incoming NEP-17 transfers. Typical
     * use cases include staking, custodial deposits, escrow mechanisms, and exchange or minting workflows where a
     * contract accepts fungible tokens and may return another asset in response.
     *
     * @see <a href="https://github.com/neo-project/proposals/blob/master/nep-27.mediawiki">NEP-27 Specification</a>
     */
    NEP_27("NEP-27"),
    /**
     * This value is required as default dummy value in the {@link io.neow3j.devpack.annotations.SupportedStandard}
     * annotation interface.
     * <p>
     * Do not use this value in your code, as it does not represent any standard. It is only used to avoid the need
     * for null values in the annotation interface.
     */
    None("");

    private final String standard;

    NeoStandard(String standard) {
        this.standard = standard;
    }

    public String getStandard() {
        return standard;
    }

}
