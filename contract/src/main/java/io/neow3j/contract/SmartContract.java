package io.neow3j.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Wallet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Represents a smart contract on the Neo blockchain and provides methods to invoke it.
 */
public class SmartContract {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected ScriptHash scriptHash;
    protected Neow3j neow;
    private NefFile nefFile;
    private ContractManifest manifest;

    /**
     * Constructs a <tt>SmartContract</tt> representing the smart contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash The smart contract's script hash.
     * @param neow       The {@link Neow3j} instance to use for invocations.
     */
    public SmartContract(ScriptHash scriptHash, Neow3j neow) {
        if (scriptHash == null) {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow == null) {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow = neow;
    }

    /**
     * Constructs a <tt>SmartContract</tt> from a NEF file and a manifest.
     *
     * @param neow         The {@link Neow3j} instance to use for deploying and invoking the
     *                     contract.
     * @param nef          The file containing the contract's code in NEF.
     * @param manifestFile The file containing the contract's manifest.
     * @throws IOException              If there is a problem reading the provided files.
     * @throws DeserializationException If the NEF file cannot be deserialized properly.
     */
    public SmartContract(Neow3j neow, File nef, File manifestFile)
            throws IOException, DeserializationException {
        this.neow = neow;
        this.nefFile = NefFile.readFromFile(nef);
        this.scriptHash = this.nefFile.getScriptHash();
        this.manifest = objectMapper.readValue(new FileInputStream(manifestFile),
                ContractManifest.class);

        if (!this.nefFile.getScriptHash().toString().equals(
                Numeric.cleanHexPrefix(this.manifest.getAbi().getHash()))) {
            throw new IllegalArgumentException("Script hash of given NEF file does not equal the "
                    + "script hash from the given manifest file.");
        }
        byte[] manifestBytes = objectMapper.writeValueAsBytes(this.manifest);
        if (manifestBytes.length > NeoConstants.MAX_MANIFEST_SIZE) {
            throw new IllegalArgumentException("The given contract manifest is to long. Manifest "
                    + "was " + manifestBytes.length + " bytes big, but a max of "
                    + NeoConstants.MAX_MANIFEST_SIZE + " is allowed.");
        }
    }

    /**
     * Initializes an invocation of the given function on this contract.
     *
     * @param function The function to invoke.
     * @return An {@link Invocation} allowing to set further details of the invocation.
     */
    public Invocation.Builder invoke(String function) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        return new Invocation.Builder(neow).withContract(scriptHash).withFunction(function);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting a String as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the string returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a
     *                                       String.
     */
    public String callFuncReturningString(String function, ContractParameter... params)
            throws UnexpectedReturnTypeException, IOException {

        StackItem item = invokeFunction(function, params).getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsString();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_STRING);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting an integer as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the integer returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an
     *                                       integer.
     */
    public BigInteger callFuncReturningInt(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item = invokeFunction(function, params).getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.INTEGER,
                StackItemType.BYTE_STRING);
    }

    protected NeoInvokeFunction invokeFunction(String function, ContractParameter... params)
            throws IOException {

        if (params.length > 0) {
            return invoke(function).withParameters(params).invokeFunction();
        } else {
            return invoke(function).invokeFunction();
        }
    }

    public ScriptHash getScriptHash() {
        return this.scriptHash;
    }

    /**
     * Deploys this contract by creating a deployment transaction and sending it to the neo-node
     *
     * @param sender The account paying for the deployment fees.
     * @param wallet The wallet containing the sender account.
     * @return The Neo node's response.
     * @throws IOException If something goes wrong when communicating with the Neo node.
     */
    public NeoSendRawTransaction deploy(ScriptHash sender, Wallet wallet) throws IOException {

        byte[] script = new ScriptBuilder()
                .pushData(objectMapper.writeValueAsBytes(this.manifest))
                .pushData(this.nefFile.getScript())
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CREATE)
                .toArray();

        return new Invocation.Builder(neow)
                .withScript(script)
                .withSender(sender)
                .withWallet(wallet)
                .build()
                .sign()
                .send();
    }
}
