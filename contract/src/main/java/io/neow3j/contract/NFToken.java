package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NFTokenProperties;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a NEP-11 Non-Fungible Token contract and provides methods to invoke it.
 */
public class NFToken extends Token {
//    Example implementation for Neo2:
//    https://gist.github.com/hal0x2328/3237fd9f61132cea6b6dd43899947753

    private static final String TRANSFER = "transfer";
    private static final String OWNER_OF = "ownerOf";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TOKENS_OF = "tokensOf";
    private static final String PROPERTIES = "properties";

    /**
     * Constructs a new <tt>Nep5Token</tt> representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash.
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NFToken(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Transfers the tokenId with {@code tokenId} to the account {@code to}.
     *
     * @param to the receiver of the tokenId.
     * @param tokenId the script hash of the tokenId.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, ScriptHash to, ScriptHash tokenId) throws IOException {
        int decimals = getDecimals();
        if (decimals != 0) {
            throw new IllegalStateException("This method is only implemented on NF tokens that are " +
                    "divisible. This tokenId has " + decimals + " decimals.");
        }

        List<ScriptHash> owners = ownerOf(tokenId);
        if (owners.size() != 1) {
            throw new IllegalStateException("The tokenId with id " + tokenId.toString() + " has " +
                    owners.size() + " owners. To transfer fractions use the method transferFractions.");
        }

        ScriptHash tokenOwner = owners.get(0);
        if (!wallet.holdsAccount(tokenOwner)) {
            throw new IllegalArgumentException("The provided wallet does not contain the account that " +
                    "owns the tokenId " + tokenId.toString() + ". The address of the owner of this tokenId " +
                    "is " + tokenOwner.toAddress() + ".");
        }

        return invokeFunction(TRANSFER,
                ContractParameter.hash160(to),
                ContractParameter.hash160(tokenId))
                .wallet(wallet)
                .signers(Signer.calledByEntry(tokenOwner));
    }

    /**
     * Transfers the tokenId with {@code tokenId} to the account {@code to}.
     *
     * @param wallet the wallet that holds the {@code from} account.
     * @param from the account to send the tokenId fractions from.
     * @param to the receiver of the tokenId fraction.
     * @param amount the fraction to be transferred.
     * @param tokenId the script hash of the tokenId.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFraction(Wallet wallet, ScriptHash from, ScriptHash to,
            BigDecimal amount, ScriptHash tokenId) throws IOException {
        List<ScriptHash> owners = ownerOf(tokenId);

        if (owners.stream().noneMatch(from::equals)) {
            throw new IllegalStateException("The account " + from.toAddress() + " is not an owner " +
                    "of the token with script hash " + tokenId.toString() + ".");
        }

        if (!wallet.holdsAccount(from)) {
            throw new IllegalArgumentException("The provided wallet does not contain the provided from " +
                    "account.");
        }

        BigInteger fractions = getAmountAsBigInteger(amount);
        System.out.println(fractions);

        return invokeFunction(TRANSFER,
                ContractParameter.hash160(from),
                ContractParameter.hash160(to),
                ContractParameter.integer(fractions),
                ContractParameter.hash160(tokenId))
                .wallet(wallet)
                .signers(Signer.calledByEntry(from));
    }

    /**
     * Gets the owner(s) of the token with script hash {@code tokenId}.
     *
     * @param tokenId the script hash of the tokenId.
     * @return a transaction builder
     */
    // According to the not yet final NEP-11 proposal, this method returns an enumerator that contains all
    //  the co-owners that own the specified token. Hence, this method expects an array stack item
    //  containing the co-owners.
    // TODO: 15.10.20 Michael: Adapt this method as soon as an implementation of the NEP-11 proposal exists.
    public List<ScriptHash> ownerOf(ScriptHash tokenId) throws IOException {
        return callFunctionReturningListOfScriptHashes(OWNER_OF,
                Arrays.asList(ContractParameter.hash160(tokenId)));
    }

    private List<ScriptHash> callFunctionReturningListOfScriptHashes(String function,
            List<ContractParameter> params) throws IOException {

        StackItem arrayItem = callInvokeFunction(function, params).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }
        List<ScriptHash> scriptHashes = new ArrayList<>();
        for (StackItem item : arrayItem.asArray().getValue()) {
            scriptHashes.add(extractScriptHash(item));
        }
        return scriptHashes;
    }

    private ScriptHash extractScriptHash(StackItem item) {
        if (!item.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(item.getType(),
                    StackItemType.BYTE_STRING);
        }
        try {
            return ScriptHash.fromAddress(item.asByteString().getAsAddress());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Byte array return type did not contain "
                    + "script hash in expected format.", e);
        }
    }

    /**
     * Gets the balance of the given token for the given account script hash.
     * <p>
     * The balance is returned in token fractions. E.g., a balance of 0.5 of a token with 2 decimals
     * is returned as 50 (= 0.5 * 10^2) token fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param owner the script hash of the account to fetch the balance for.
     * @param tokenId the token id.
     * @return the token balance of the given account.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger balanceOf(ScriptHash owner, ScriptHash tokenId) throws IOException {
        return callFuncReturningInt(BALANCE_OF,
                ContractParameter.hash160(owner),
                ContractParameter.hash160(tokenId));
    }

    /**
     * Gets all the token script hashes owned by the given account.
     *
     * @param owner the account.
     * @return a list of all tokens that are owned by the given account.
     */
    public List<ScriptHash> tokensOf(ScriptHash owner) throws IOException {
        return callFunctionReturningListOfScriptHashes(TOKENS_OF,
                Arrays.asList(ContractParameter.hash160(owner)));
    }

    /**
     * Gets the properties of the given token.
     *
     * @param tokenId the token script hash.
     * @return the properties of the token.
     */
    public NFTokenProperties properties(byte[] tokenId) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES, Arrays.asList(ContractParameter.byteArray(tokenId)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsJson(NFTokenProperties.class);
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_STRING);
    }
}
