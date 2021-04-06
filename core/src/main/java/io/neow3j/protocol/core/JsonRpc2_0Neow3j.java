package io.neow3j.protocol.core;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoBlockHeaderCount;
import io.neow3j.protocol.core.methods.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetCommittee;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetNativeContracts;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetProof;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStateHeight;
import io.neow3j.protocol.core.methods.response.NeoGetStateRoot;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeContractVerify;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.NeoVerifyProof;
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.protocol.core.methods.response.TransactionSigner;
import io.neow3j.protocol.rx.JsonRpc2_0Rx;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Async;
import io.reactivex.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Neow3j extends Neow3j {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final Neow3jService neow3jService;
    private final JsonRpc2_0Rx neow3jRx;
    private final long blockTime;
    private final ScheduledExecutorService scheduledExecutorService;

    public JsonRpc2_0Neow3j(Neow3jService neow3jService) {
        this(neow3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0Neow3j(Neow3jService neow3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.neow3jService = neow3jService;
        this.neow3jRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    // Blockchain Methods

    /**
     * Gets the hash of the latest block in the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHash> getBestBlockHash() {
        return new Request<>(
                "getbestblockhash",
                emptyList(),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the block hash of the corresponding block based on the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHash> getBlockHash(BigInteger blockIndex) {
        return new Request<>(
                "getblockhash",
                singletonList(blockIndex),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the corresponding block information according to the specified block hash.
     *
     * @param blockHash                    the block hash.
     * @param returnFullTransactionObjects whether to get block information with all transaction
     *                                     objects or just the block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(Hash256 blockHash,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(blockHash, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(blockHash);
        }
    }

    /**
     * Gets the corresponding block information for the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(Hash256 blockHash) {
        return new Request<>(
                "getblock",
                asList(blockHash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block information according to the specified block index.
     *
     * @param blockIndex                   the block index.
     * @param returnFullTransactionObjects whether to get block information with all transaction
     *                                     objects or just the block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(BigInteger blockIndex,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(blockIndex, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(blockIndex);
        }
    }

    /**
     * Gets the corresponding block information according to the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(BigInteger blockIndex) {
        return new Request<>(
                "getblock",
                asList(blockIndex, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the block header count of the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHeaderCount> getBlockHeaderCount() {
        return new Request<>(
                "getblockheadercount",
                emptyList(),
                neow3jService,
                NeoBlockHeaderCount.class);
    }

    /**
     * Gets the block count of the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockCount> getBlockCount() {
        return new Request<>(
                "getblockcount",
                emptyList(),
                neow3jService,
                NeoBlockCount.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(Hash256 blockHash) {
        return new Request<>(
                "getblockheader",
                asList(blockHash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(BigInteger blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(Hash256 blockHash) {
        return new Request<>(
                "getblockheader",
                asList(blockHash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(BigInteger blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the native contracts list, which includes the basic information of native contracts
     * and the contract descriptive file {@code manifest.json}.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNativeContracts> getNativeContracts() {
        return new Request<>(
                "getnativecontracts",
                emptyList(),
                neow3jService,
                NeoGetNativeContracts.class);
    }

    /**
     * Gets the contract information.
     *
     * @param contractHash the contract script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getContractState(Hash160 contractHash) {
        return new Request<>(
                "getcontractstate",
                singletonList(contractHash),
                neow3jService,
                NeoGetContractState.class);
    }

    /**
     * Gets the native contract information by its name.
     * <p>
     * This RPC only works for native contracts.
     *
     * @param nativeContractName the name of the native contract.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getNativeContractState(String nativeContractName) {
        return new Request<>(
                "getcontractstate",
                singletonList(nativeContractName),
                neow3jService,
                NeoGetContractState.class);
    }

    /**
     * Gets a list of unconfirmed or confirmed transactions in memory.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetMemPool> getMemPool() {
        return new Request<>(
                "getrawmempool",
                singletonList(1),
                neow3jService,
                NeoGetMemPool.class);
    }

    /**
     * Gets a list of confirmed transactions in memory.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawMemPool> getRawMemPool() {
        return new Request<>(
                "getrawmempool",
                emptyList(),
                neow3jService,
                NeoGetRawMemPool.class);
    }

    /**
     * Gets the corresponding transaction information based on the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransaction> getTransaction(Hash256 txHash) {
        return new Request<>(
                "getrawtransaction",
                asList(txHash, 1),
                neow3jService,
                NeoGetTransaction.class);
    }

    /**
     * Gets the corresponding transaction information based on the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(Hash256 txHash) {
        return new Request<>(
                "getrawtransaction",
                asList(txHash, 0),
                neow3jService,
                NeoGetRawTransaction.class);
    }

    /**
     * Gets the stored value according to the contract hash and the key.
     *
     * @param contractHash the contract hash.
     * @param keyHexString the key to look up in storage as a hexadecimal string.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStorage> getStorage(Hash160 contractHash, String keyHexString) {
        return new Request<>(
                "getstorage",
                asList(contractHash, Base64.encode(keyHexString)),
                neow3jService,
                NeoGetStorage.class);
    }

    /**
     * Gets the transaction height with the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransactionHeight> getTransactionHeight(Hash256 txHash) {
        return new Request<>(
                "gettransactionheight",
                singletonList(txHash),
                neow3jService,
                NeoGetTransactionHeight.class);
    }

    /**
     * Gets the validators of the next block.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNextBlockValidators> getNextBlockValidators() {
        return new Request<>(
                "getnextblockvalidators",
                emptyList(),
                neow3jService,
                NeoGetNextBlockValidators.class);
    }

    /**
     * Gets the public key list of current Neo committee members.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetCommittee> getCommittee() {
        return new Request<>(
                "getcommittee",
                emptyList(),
                neow3jService,
                NeoGetCommittee.class);
    }

    // Node Methods

    /**
     * Gets the current number of connections for the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoConnectionCount> getConnectionCount() {
        return new Request<>(
                "getconnectioncount",
                emptyList(),
                neow3jService,
                NeoConnectionCount.class);
    }

    /**
     * Gets a list of nodes that the node is currently connected or disconnected from.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetPeers> getPeers() {
        return new Request<>(
                "getpeers",
                emptyList(),
                neow3jService,
                NeoGetPeers.class);
    }

    /**
     * Gets the version information of the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetVersion> getVersion() {
        return new Request<>(
                "getversion",
                emptyList(),
                neow3jService,
                NeoGetVersion.class);
    }

    /**
     * Broadcasts a transaction over the NEO network.
     *
     * @param rawTransactionHex the raw transaction in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex) {
        return new Request<>(
                "sendrawtransaction",
                singletonList(Base64.encode(rawTransactionHex)),
                neow3jService,
                NeoSendRawTransaction.class);
    }

    /**
     * Broadcasts a new block over the NEO network.
     *
     * @param serializedBlockAsHex the block in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex) {
        return new Request<>(
                "submitblock",
                singletonList(serializedBlockAsHex),
                neow3jService,
                NeoSubmitBlock.class);
    }

    // SmartContract Methods

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified
     * contract hash.
     *
     * @param contractHash the contract hash to invoke.
     * @param functionName the function to invoke.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName,
            Signer... signers) {
        return invokeFunction(contractHash, functionName, null, signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified
     * contract hash.
     *
     * @param contractHash   the contract hash to invoke.
     * @param functionName   the function to invoke.
     * @param contractParams the parameters of the function.
     * @param signers        the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName,
            List<ContractParameter> contractParams, Signer... signers) {

        if (contractParams == null) {
            contractParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = stream(signers).map(TransactionSigner::new)
                .collect(Collectors.toList());
        List<?> params;
        if (txSigners.size() > 0) {
            params = asList(contractHash, functionName, contractParams, txSigners);
        } else {
            params = asList(contractHash, functionName, contractParams);
        }
        return new Request<>(
                "invokefunction",
                params.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                neow3jService,
                NeoInvokeFunction.class);
    }

    /**
     * Invokes a script.
     *
     * @param script  the script to invoke.
     * @param signers the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeScript> invokeScript(String script, Signer... signers) {
        List<?> params;
        if (signers.length > 0) {
            params = asList(script, stream(signers)
                    .map(TransactionSigner::new)
                    .collect(Collectors.toList()));
        } else {
            params = singletonList(script);
        }
        return new Request<>(
                "invokescript",
                params,
                neow3jService,
                NeoInvokeScript.class);
    }

    /**
     * Invokes a contract in verification mode.
     * <p>
     * Requires an open wallet on the Neo node that contains the accounts for the signers.
     *
     * @param contractHash the contract hash.
     * @param methodParams a list of parameters of the verify function.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeContractVerify> invokeContractVerify(Hash160 contractHash,
            List<ContractParameter> methodParams, Signer... signers) {

        if (methodParams == null) {
            methodParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = stream(signers)
                .map(TransactionSigner::new).collect(Collectors.toList());
        List<?> params = asList(contractHash, methodParams, txSigners);
        return new Request<>(
                "invokecontractverify",
                params.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                neow3jService,
                NeoInvokeContractVerify.class);
    }

    /**
     * Gets the unclaimed GAS of the account with the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetUnclaimedGas> getUnclaimedGas(Hash160 scriptHash) {
        return new Request<>(
                "getunclaimedgas",
                singletonList(scriptHash.toAddress()),
                neow3jService,
                NeoGetUnclaimedGas.class);
    }

    // Utilities Methods

    /**
     * Gets a list of plugins loaded by the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoListPlugins> listPlugins() {
        return new Request<>(
                "listplugins",
                emptyList(),
                neow3jService,
                NeoListPlugins.class);
    }

    /**
     * Verifies whether the address is a valid NEO address.
     *
     * @param address the address to verify.
     * @return the request object.
     */
    @Override
    public Request<?, NeoValidateAddress> validateAddress(String address) {
        return new Request<>(
                "validateaddress",
                singletonList(address),
                neow3jService,
                NeoValidateAddress.class);
    }

    // Wallet Methods

    /**
     * Closes the current wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoCloseWallet> closeWallet() {
        return new Request<>(
                "closewallet",
                emptyList(),
                neow3jService,
                NeoCloseWallet.class);
    }

    /**
     * Exports the private key of the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoDumpPrivKey> dumpPrivKey(Hash160 scriptHash) {
        return new Request<>(
                "dumpprivkey",
                singletonList(scriptHash.toAddress()),
                neow3jService,
                NeoDumpPrivKey.class);
    }

    /**
     * Gets the wallet balance of the corresponding token.
     *
     * @param tokenHash the token hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletBalance> getWalletBalance(Hash160 tokenHash) {
        return new Request<>(
                "getwalletbalance",
                singletonList(tokenHash),
                neow3jService,
                NeoGetWalletBalance.class);
    }

    /**
     * Creates a new address.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNewAddress> getNewAddress() {
        return new Request<>(
                "getnewaddress",
                emptyList(),
                neow3jService,
                NeoGetNewAddress.class);
    }

    /**
     * Gets the amount of unclaimed GAS in the wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas() {
        return new Request<>(
                "getwalletunclaimedgas",
                emptyList(),
                neow3jService,
                NeoGetWalletUnclaimedGas.class);
    }

    /**
     * Imports a private key to the wallet.
     *
     * @param privateKeyInWIF the private key in WIF-format.
     * @return the request object.
     */
    @Override
    public Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF) {
        return new Request<>(
                "importprivkey",
                singletonList(privateKeyInWIF),
                neow3jService,
                NeoImportPrivKey.class);
    }

    /**
     * Calculates the network fee for the specified transaction.
     *
     * @param txHex the transaction in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoCalculateNetworkFee> calculateNetworkFee(String txHex) {
        return new Request<>(
                "calculatenetworkfee",
                singletonList(Base64.encode(txHex)),
                neow3jService,
                NeoCalculateNetworkFee.class);
    }

    /**
     * Lists all the addresses in the current wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoListAddress> listAddress() {
        return new Request<>(
                "listaddress",
                emptyList(),
                neow3jService,
                NeoListAddress.class);
    }

    /**
     * Opens the specified wallet.
     *
     * @param walletPath the wallet file path.
     * @param password   the password for the wallet.
     * @return the request object.
     */
    @Override
    public Request<?, NeoOpenWallet> openWallet(String walletPath, String password) {
        return new Request<>(
                "openwallet",
                asList(walletPath, password),
                neow3jService,
                NeoOpenWallet.class);
    }

    /**
     * Transfers an amount of an asset from an address to another address.
     *
     * @param tokenHash the token hash of the NEP-17 contract.
     * @param from      the transferring account's script hash.
     * @param to        the recipient.
     * @param amount    the transfer amount in token fractions.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(Hash160 tokenHash, Hash160 from, Hash160 to,
            BigInteger amount) {
        return new Request<>(
                "sendfrom",
                asList(tokenHash, from.toAddress(), to.toAddress(), amount),
                neow3jService,
                NeoSendFrom.class);
    }

    /**
     * Transfers an amount of a token from an address to another address.
     *
     * @param from        the transferring account's script hash.
     * @param txSendAsset a {@link TransactionSendAsset} object containing the token hash, the
     *                    transferring account's script hash and the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(Hash160 from, TransactionSendAsset txSendAsset) {
        return sendFrom(from, txSendAsset.getAsset(), Hash160.fromAddress(txSendAsset.getAddress()),
                new BigInteger(txSendAsset.getValue()));
    }

    /**
     * Initiates multiple transfers to multiple addresses from the open wallet in a transaction.
     *
     * @param txSendAsset a list of {@link TransactionSendAsset} objects, that each contains the
     *                    token asset, destination address and transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset) {
        return new Request<>(
                "sendmany",
                singletonList(txSendAsset.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Initiates multiple transfers to multiple addresses from one specific address in a
     * transaction.
     *
     * @param fromAddress the transferring address.
     * @param txSendAsset a list of {@link TransactionSendAsset} objects, that each contains the
     *                    token asset, destination address and transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(String fromAddress,
            List<TransactionSendAsset> txSendAsset) {

        return new Request<>(
                "sendmany",
                asList(fromAddress,
                        txSendAsset.stream().filter(Objects::nonNull).collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Transfers an amount of a token asset to another address.
     *
     * @param tokenHash the token hash of the NEP-17 contract.
     * @param to        the recipient.
     * @param amount    the transfer amount in token fractions.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(Hash160 tokenHash, Hash160 to,
            BigInteger amount) {
        return new Request<>(
                "sendtoaddress",
                Stream.of(tokenHash, to, amount)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendToAddress.class);
    }

    /**
     * Transfers an amount of a token asset to another address.
     *
     * @param txSendAsset a {@link TransactionSendAsset} object containing the token hash, the
     *                    recipient and the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset) {
        return sendToAddress(txSendAsset.getAsset(), Hash160.fromAddress(txSendAsset.getAddress()),
                new BigInteger(txSendAsset.getValue()));
    }

    // ApplicationLogs

    /**
     * Gets the application logs of the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(Hash256 txHash) {
        return new Request<>(
                "getapplicationlog",
                singletonList(txHash),
                neow3jService,
                NeoGetApplicationLog.class);
    }

    // RpcNep17Tracker

    /**
     * Gets the balance of all NEP-17 token assets in the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Balances> getNep17Balances(Hash160 scriptHash) {
        return new Request<>(
                "getnep17balances",
                singletonList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep17Balances.class);
    }

    /**
     * Gets all the NEP-17 transaction information occurred in the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash) {
        return new Request<>(
                "getnep17transfers",
                singletonList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified script hash since the
     * specified time.
     *
     * @param scriptHash the account's script hash.
     * @param from       the timestamp transactions occurred since.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from) {
        return new Request<>(
                "getnep17transfers",
                asList(scriptHash.toAddress(), from.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified script hash in the
     * specified time range.
     *
     * @param scriptHash the account's script hash.
     * @param from       the start timestamp.
     * @param until      the end timestamp.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from,
            Date until) {
        return new Request<>(
                "getnep17transfers",
                asList(scriptHash.toAddress(), from.getTime(), until.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    // StateService

    /**
     * Gets the state root by the block height.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStateRoot> getStateRoot(BigInteger blockIndex) {
        return new Request<>(
                "getstateroot",
                singletonList(blockIndex),
                neow3jService,
                NeoGetStateRoot.class);
    }

    /**
     * Gets the proof based on the root hash, the contract hash and the storage key.
     *
     * @param rootHash      the root hash.
     * @param contractHash  the contract hash.
     * @param storageKeyHex the storage key.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetProof> getProof(Hash256 rootHash, Hash160 contractHash,
            String storageKeyHex) {
        return new Request<>(
                "getproof",
                asList(rootHash, contractHash, Base64.encode(storageKeyHex)),
                neow3jService,
                NeoGetProof.class);
    }

    /**
     * Verifies the proof data and gets the value of the storage corresponding to the key.
     *
     * @param rootHash     the root hash.
     * @param proofDataHex the proof data of the state root.
     * @return the request object.
     */
    @Override
    public Request<?, NeoVerifyProof> verifyProof(Hash256 rootHash, String proofDataHex) {
        return new Request<>(
                "verifyproof",
                asList(rootHash, Base64.encode(proofDataHex)),
                neow3jService,
                NeoVerifyProof.class);
    }

    /**
     * Gets the state root height.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStateHeight> getStateHeight() {
        return new Request<>(
                "getstateheight",
                emptyList(),
                neow3jService,
                NeoGetStateHeight.class);
    }

    // Neow3j Rx Convenience methods:

    @Override
    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects) {
        return neow3jRx.blockObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {
        return neow3jRx.catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BigInteger startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<NeoGetBlock> subscribeToNewBlocksObservable(boolean fullTransactionObjects)
            throws IOException {

        return neow3jRx.subscribeToNewBlocksObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            neow3jService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close neow3j service", e);
        }
    }

}
