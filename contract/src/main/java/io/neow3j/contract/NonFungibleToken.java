package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.MapStackItem;
import io.neow3j.protocol.core.methods.response.TokenState;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.hash160;

/**
 * Represents a NEP-11 non-fungible token contract and provides methods to
 * invoke it.
 */
public class NonFungibleToken extends Token {

    private static final String OWNER_OF = "ownerOf";
    private static final String PROPERTIES = "properties";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code NFT} representing the contract with the given
     * script hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash.
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NonFungibleToken(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    @Override
    public int getDecimals() {
        return 0;
    }

    /**
     * Transfers the token with {@code tokenID} to the account {@code to}.
     *
     * @param wallet the wallet that holds the account of the token owner.
     * @param to the receiver of the token.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException  if there was a problem fetching information from
     *                      the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, ScriptHash to,
            byte[] tokenID) throws IOException {
        ScriptHash tokenOwner = ownerOf(tokenID);
        if (!wallet.holdsAccount(tokenOwner)) {
            throw new IllegalArgumentException("The provided wallet does not" +
                    " contain the account that owns the token with ID " +
                    Numeric.toHexString(tokenID) + ". The address of the " +
                    " owner of this token is " + tokenOwner.toAddress() + ".");
        }

        return invokeFunction(TRANSFER,
                hash160(to),
                byteArray(tokenID))
                .wallet(wallet)
                .signers(Signer.calledByEntry(tokenOwner));
    }

    /**
     * Gets the owner of the token with {@code tokenID}.
     *
     * @param tokenID the token ID.
     * @return a list of owners of the token.
     * @throws IOException if an error occurs when interacting with the
     *                     Neo node.
     */
    public ScriptHash ownerOf(byte[] tokenID) throws IOException {
        return callFunctionReturningScriptHash(OWNER_OF,
                Arrays.asList(byteArray(tokenID)));
    }

    private ScriptHash callFunctionReturningScriptHash(String function,
            List<ContractParameter> params) throws IOException {

        StackItem stackItem = callInvokeFunction(function, params)
                .getInvocationResult().getStack().get(0);
        return extractScriptHash(stackItem);
    }

    private ScriptHash extractScriptHash(StackItem item) {
        if (!item.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(item.getType(),
                    StackItemType.BYTE_STRING);
        }
        try {
            return ScriptHash.fromAddress(item.asByteString().getAsAddress());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Return type " +
                    "did not contain script hash in expected format.", e);
        }
    }

    /**
     * Gets the balance of the token with {@code tokenID} for the given account.
     * <p>
     * The balance is returned in token fractions. E.g., a balance of 0.5 of a
     * token with 2 decimals is returned as 50 (= 0.5 * 10^2) token fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called
     * requests are send to the neo-node.
     *
     * @param owner the script hash of the account to fetch the balance for.
     * @return the token balance of the given account.
     * @throws IOException if there was a problem fetching information from
     *                     the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not
     *                                       return something interpretable as
     *                                       a number.
     */
    public BigInteger balanceOf(ScriptHash owner)
            throws IOException {
        return callFuncReturningInt(BALANCE_OF,
                hash160(owner));
    }

    /**
     * Gets the properties of the token with {@code tokenID}.
     *
     * @param tokenID the token ID.
     * @return the properties of the token.
     * @throws IOException if an error occurs when interacting with the
     *                     Neo node.
     */
    public TokenState properties(byte[] tokenID) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES,
                Arrays.asList(byteArray(tokenID)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.MAP)) {
            MapStackItem mapStackItem = item.asMap();
            return new TokenState(
                    mapStackItem.get("name").asByteString().getAsString(),
                    mapStackItem.get("description").asByteString().getAsString()
            );
        }
        throw new UnexpectedReturnTypeException(item.getType(),
                StackItemType.MAP);
    }
}
