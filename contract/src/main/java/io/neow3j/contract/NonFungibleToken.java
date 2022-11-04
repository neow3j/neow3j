package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.ContractSigner;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.StackItemType.ANY;
import static io.neow3j.types.StackItemType.MAP;
import static java.util.Arrays.asList;

/**
 * Represents a NEP-11 non-fungible token contract and provides methods to invoke it.
 */
@SuppressWarnings("unchecked")
public class NonFungibleToken extends Token {

    private static final String OWNER_OF = "ownerOf";
    private static final String TOKENS_OF = "tokensOf";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";
    private static final String TOKENS = "tokens";
    private static final String PROPERTIES = "properties";

    /**
     * Constructs a new {@code NFT} representing the contract with the given script hash. Uses the given
     * {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash.
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public NonFungibleToken(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    // region Token methods

    /**
     * Gets the total amount of NFTs owned by the {@code owner}.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the Neo node.
     *
     * @param owner the script hash of the account to fetch the balance for.
     * @return the token balance of the given account.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger balanceOf(Hash160 owner) throws IOException {
        return callFunctionReturningInt(BALANCE_OF, hash160(owner));
    }

    // endregion Token methods
    // region NFT methods

    /**
     * Gets an iterator over the token ids of the tokens that are owned by the {@code owner}.
     *
     * @param owner the owner of the tokens.
     * @return a list of token ids that are owned by the specified owner.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<byte[]> tokensOf(Hash160 owner) throws IOException {
        return callFunctionReturningIterator(StackItem::getByteArray, TOKENS_OF, hash160(owner));
    }

    // endregion NFT methods
    // region Non-divisible NFT methods

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * The token owner is set as signer of the transaction. The returned builder is ready to be signed and sent.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param from    the account of the token owner.
     * @param to      the receiver of the token.
     * @param tokenId the token id.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, byte[] tokenId) throws IOException {
        return transfer(from, to, tokenId, null);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * The token owner is set as a {@code calledByEntry} signer of the transaction. The returned builder is ready to
     * be signed and sent.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param from    the account of the token owner.
     * @param to      the receiver of the token.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method of the receiving smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, byte[] tokenId, ContractParameter data)
            throws IOException {
        throwIfSenderIsNotOwner(from.getScriptHash(), tokenId);
        return transfer(to, tokenId, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param to      the receiver of the token.
     * @param tokenId the token id.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 to, byte[] tokenId) throws IOException {
        return transfer(to, tokenId, null);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the owner is a contract.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param to      the receiver of the token.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 to, byte[] tokenId, ContractParameter data)
            throws IOException {
        throwIfDivisibleNFT();
        return invokeFunction(TRANSFER, hash160(to), byteArray(tokenId), data);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * The token owner is set as signer of the transaction. The returned builder is ready to be signed and sent.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param from    the account of the token owner.
     * @param to      the NNS domain name to resolve.
     * @param tokenId the token id.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, byte[] tokenId)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from, to, tokenId, null);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * The token owner is set as a {@code calledByEntry} signer of the transaction. The returned builder is ready to
     * be signed and sent.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param from    the account of the token owner.
     * @param to      the NNS domain name to resolve.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method of the receiving smart contract.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, byte[] tokenId, ContractParameter data)
            throws IOException, UnresolvableDomainNameException {
        throwIfSenderIsNotOwner(from.getScriptHash(), tokenId);
        return transfer(to, tokenId, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param to      the NNS domain name to resolve.
     * @param tokenId the token id.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(NNSName to, byte[] tokenId) throws IOException, UnresolvableDomainNameException {
        return transfer(to, tokenId, null);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the owner is a contract.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param to      the NNS domain name to resolve.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(NNSName to, byte[] tokenId, ContractParameter data)
            throws IOException, UnresolvableDomainNameException {
        throwIfDivisibleNFT();
        Hash160 toScriptHash = resolveNNSTextRecord(to);
        return invokeFunction(TRANSFER, hash160(toScriptHash), byteArray(tokenId), data);
    }

    /**
     * Builds a script that invokes the transfer method on this non-fungible token.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param to      the recipient.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transfer script.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public byte[] buildNonDivisibleTransferScript(Hash160 to, byte[] tokenId, ContractParameter data)
            throws IOException {
        throwIfDivisibleNFT();
        return buildInvokeFunctionScript(TRANSFER, hash160(to), byteArray(tokenId), data);
    }

    /**
     * Gets the owner of the token with {@code tokenId}.
     * <p>
     * This method is intended to be used for non-divisible NFTs only.
     *
     * @param tokenId the token id.
     * @return the token owner.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Hash160 ownerOf(byte[] tokenId) throws IOException {
        throwIfDivisibleNFT();
        return callFunctionReturningScriptHash(OWNER_OF, byteArray(tokenId));
    }

    private void throwIfDivisibleNFT() throws IOException {
        if (getDecimals() != 0) {
            throw new IllegalStateException("This method is only intended for non-divisible NFTs.");
        }
    }

    private void throwIfSenderIsNotOwner(Hash160 from, byte[] tokenId) throws IOException {
        Hash160 tokenOwner = ownerOf(tokenId);
        if (!tokenOwner.equals(from)) {
            throw new IllegalArgumentException("The provided from account is not the owner of this token.");
        }
    }

    // endregion Non-divisible NFT methods
    // region Divisible NFT methods

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * The sender is set as signer of the transaction. The returned builder is ready to be signed and sent.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount, byte[] tokenID) throws IOException {
        return transfer(from, to, amount, tokenID, null);
    }

    /**
     * Creates a transaction script to transfer an amount of a divisible non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * The sender is set as a {@code calledByEntry} signer of the transaction. The returned builder is ready to be
     * signed and sent.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount, byte[] tokenId,
            ContractParameter data) throws IOException {
        return transfer(from.getScriptHash(), to, amount, tokenId, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 from, Hash160 to, BigInteger amount, byte[] tokenID) throws IOException {
        return transfer(from, to, amount, tokenID, null);
    }

    /**
     * Creates a transaction script to transfer an amount of a divisible non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 from, Hash160 to, BigInteger amount, byte[] tokenId,
            ContractParameter data) throws IOException {
        throwIfNonDivisibleNFT();
        return invokeFunction(TRANSFER, hash160(from), hash160(to), integer(amount), byteArray(tokenId), data);
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * The sender is set as signer of the transaction. The returned builder is ready to be signed and sent.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, BigInteger amount, byte[] tokenID)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from, to, amount, tokenID, null);
    }

    /**
     * Creates a transaction script to transfer an amount of a divisible non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * The sender is set as a {@code calledByEntry} signer of the transaction. The returned builder is ready to be
     * signed and sent.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, BigInteger amount, byte[] tokenId,
            ContractParameter data) throws IOException, UnresolvableDomainNameException {
        return transfer(from.getScriptHash(), to, amount, tokenId, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transaction script to transfer a non-fungible token and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Hash160 from, NNSName to, BigInteger amount, byte[] tokenID)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from, to, amount, tokenID, null);
    }

    /**
     * Creates a transaction script to transfer an amount of a divisible non-fungible token and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender of the token amount.
     * @param to      the receiver of the token amount.
     * @param amount  the fraction amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onNEP11Payment} method if the receiver is a smart contract.
     * @return a transaction builder.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Hash160 from, NNSName to, BigInteger amount, byte[] tokenId,
            ContractParameter data) throws IOException, UnresolvableDomainNameException {
        throwIfNonDivisibleNFT();
        Hash160 toScriptHash = resolveNNSTextRecord(to);
        return invokeFunction(TRANSFER, hash160(from), hash160(toScriptHash), integer(amount), byteArray(tokenId),
                data);
    }

    /**
     * Builds a script that invokes the transfer method on this non-fungible token.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param from    the sender.
     * @param to      the recipient.
     * @param amount  the amount to transfer.
     * @param tokenId the token id.
     * @param data    the data that is passed to the {@code onPayment} method if the recipient is a smart contract.
     * @return a transfer script.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public byte[] buildDivisibleTransferScript(Hash160 from, Hash160 to, BigInteger amount, byte[] tokenId,
            ContractParameter data) throws IOException {
        throwIfNonDivisibleNFT();
        return buildInvokeFunctionScript(TRANSFER, hash160(from), hash160(to), integer(amount), byteArray(tokenId),
                data);
    }

    /**
     * Gets an iterator of the owners of the token with {@code tokenId}.
     * <p>
     * Traverse the returned iterator with {@link Iterator#traverse(int)} to retrieve the owners.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param tokenId the token id.
     * @return a list of owners of the token.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<Hash160> ownersOf(byte[] tokenId) throws IOException {
        throwIfNonDivisibleNFT();
        return callFunctionReturningIterator(i -> Hash160.fromAddress(i.getAddress()), OWNER_OF, byteArray(tokenId));
    }

    private void throwIfNonDivisibleNFT() throws IOException {
        if (getDecimals() == 0) {
            throw new IllegalStateException("This method is only intended for divisible NFTs.");
        }
    }

    /**
     * Gets the balance of the token with {@code tokenId} for the given account.
     * <p>
     * The balance is returned in token fractions. E.g., a balance of 0.5 of a token with 2 decimals is returned as
     * 50 (= 0.5 * 10^2) token fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the Neo node.
     * <p>
     * This method is intended to be used for divisible NFTs only.
     *
     * @param owner   the script hash of the account to fetch the balance for.
     * @param tokenId the token id.
     * @return the token balance of the given account.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger balanceOf(Hash160 owner, byte[] tokenId) throws IOException {
        throwIfNonDivisibleNFT();
        return callFunctionReturningInt(BALANCE_OF, hash160(owner), byteArray(tokenId));
    }

    // endregion Divisible NFT methods
    // region Optional methods

    /**
     * Gets an iterator of the tokens that exist on this contract.
     * <p>
     * Traverse the returned iterator with {@link Iterator#traverse(int)} to retrieve the owners.
     * <p>
     * This method is optional for the NEP-11 standard.
     *
     * @return an iterator of the tokens that exist on this contract.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<byte[]> tokens() throws IOException {
        return callFunctionReturningIterator(StackItem::getByteArray, TOKENS);
    }

    /**
     * Gets the properties of the token with {@code tokenId}.
     * <p>
     * This method is optional for the NEP-11 standard.
     * <p>
     * Use this method if the token's properties only contain {@code String} values. For custom value types, use the
     * method {@link #customProperties(byte[]) customProperties}.
     *
     * @param tokenId the token id.
     * @return the properties of the token.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Map<String, String> properties(byte[] tokenId) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES, asList(byteArray(tokenId)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(MAP)) {
            return deserializeProperties(item);
        }
        throw new UnexpectedReturnTypeException(item.getType(), MAP);
    }

    // Deserializes the properties into a map. Expects the values in the stack item map to be of type String. If a
    // value is of type Any and its value is null, null is added to the map. In other cases, getString() might throw
    // a StackItemCastException.
    private Map<String, String> deserializeProperties(StackItem item) {
        Map<String, String> map = new HashMap<>();
        for (StackItem k : item.getMap().keySet()) {
            StackItem valueStackItem = item.getMap().get(k);
            String value;
            if (valueStackItem.getType().equals(ANY) && valueStackItem.getValue() == null) {
                value = null;
            } else {
                value = valueStackItem.getString();
            }
            map.put(k.getString(), value);
        }
        return map;
    }

    /**
     * Gets the properties of the token with {@code tokenId}.
     * <p>
     * This method is optional for the NEP-11 standard.
     * <p>
     * Use this method to handle custom value types in the token's property values.
     *
     * @param tokenId the token id.
     * @return the properties of the token.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Map<String, StackItem> customProperties(byte[] tokenId) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES, asList(byteArray(tokenId)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(MAP)) {
            return item.getMap().entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey().getString(),
                    Map.Entry::getValue
            ));
        }
        throw new UnexpectedReturnTypeException(item.getType(), MAP);
    }

    // endregion Optional methods

}
