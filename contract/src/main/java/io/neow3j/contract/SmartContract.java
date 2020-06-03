package io.neow3j.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
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
     * Initializes an invocation of the given function on this contract.
     *
     * @param function The function to invoke.
     * @return An {@link InvocationBuilder} allowing to set further details of the invocation.
     */
    public InvocationBuilder invoke(String function) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        return new InvocationBuilder(neow, scriptHash, function);
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

        StackItem item = callFunction(function, params).getInvocationResult().getStack().get(0);
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

        StackItem item = callFunction(function, params).getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.INTEGER,
                StackItemType.BYTE_STRING);
    }

    protected NeoInvokeFunction callFunction(String function, ContractParameter... params)
            throws IOException {

        if (params.length > 0) {
            return invoke(function).withParameters(params).call();
        } else {
            return invoke(function).call();
        }
    }

    public ScriptHash getScriptHash() {
        return this.scriptHash;
    }

    public NeoSendRawTransaction deploy(File nefFile, File manifestFile, ScriptHash sender,
            Wallet wallet) throws IOException {
        byte[] nefBytes = readNefFile(nefFile);

        ContractManifest manifest = objectMapper.readValue(new FileInputStream(manifestFile),
                ContractManifest.class);

        if (!ScriptHash.fromScript(nefBytes).toString().equals(manifest.getAbi().getHash())) {
            throw new IllegalArgumentException("Script hash of given NEF file does not equal the "
                    + "script hash from the given manifest file.");
        }
        byte[] manifestBytes = objectMapper.writeValueAsBytes(manifest);
        if (manifestBytes.length > NeoConstants.MAX_MANIFEST_SIZE) {
            throw new IllegalArgumentException("The given contract manifest is to long. Manifest "
                    + "was " + manifestBytes.length + " bytes big, but a max of "
                    + NeoConstants.MAX_MANIFEST_SIZE + " is allowed.");
        }
        byte[] script = new ScriptBuilder()
                .pushData(manifestBytes)
                .pushData(nefBytes)
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CREATE)
                .toArray();

        // TODO: Build and send a transaction.
        return null;
    }

    private byte[] readNefFile(File nefFile) throws IOException {
        int nefFileSize = (int) nefFile.length();
        if (nefFileSize > 0x100000) {
            // This maximum size was taken from the neo-core code.
            throw new IllegalArgumentException(
                    "The given NEF file is too long. File was " + nefFileSize + " bytes big, but a "
                            + "max of 2^20 bytes is allowed.");
        }
        byte[] nefBytes = new byte[nefFileSize];
        try (FileInputStream nefStream = new FileInputStream(nefFile)) {
            nefStream.read(nefBytes);
        }
        return nefBytes;
    }
}
