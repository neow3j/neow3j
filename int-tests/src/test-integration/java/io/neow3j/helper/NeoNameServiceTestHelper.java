package io.neow3j.helper;

import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.NeoNameService;
import io.neow3j.contract.types.NNSName;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Files;
import io.neow3j.wallet.Account;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.contract.SmartContract.calcContractHash;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;

public class NeoNameServiceTestHelper {

    /**
     * Deploys the official NNS contract and sets the NNS resolver of the neow3j instance to its script hash.
     *
     * @param neow3j           the neow3j instance.
     * @param committeeAccount the committee account that is allowed to deploy the NNS contract.
     * @param defaultAccount   the signing account for the committee multi-sig account.
     * @return the script hash of the deployed NNS contract.
     * @throws Throwable if something goes wrong when deploying the NNS contract.
     */
    public static void deployNNS(Neow3j neow3j, Account committeeAccount, Account defaultAccount) throws Throwable {
        String nameServiceNef = "contracts/NameService.nef";
        String nameServiceManifest = "contracts/NameService.manifest.json";

        URL nefUrl = NeoNameServiceTestHelper.class.getClassLoader().getResource(nameServiceNef);
        URL manifestUrl = NeoNameServiceTestHelper.class.getClassLoader().getResource(nameServiceManifest);
        deployNNS(neow3j, nefUrl, manifestUrl, committeeAccount, defaultAccount);
    }

    /**
     * Deploys the official NNS contract and sets the NNS resolver of the neow3j instance to its script hash.
     *
     * @param neow3j          the neow3j instance.
     * @param nefURL          the pointer to the nef file of the NNS contract.
     * @param manifestURL     the pointer to the manifest file of the NNS contract.
     * @param committee       the committee account that is allowed to deploy the NNS contract.
     * @param signingAccounts the signing accounts for the committee multi-sig account.
     * @return the script hash of the deployed NNS contract.
     * @throws Throwable if something goes wrong when deploying the NNS contract.
     */
    public static void deployNNS(Neow3j neow3j, URL nefURL, URL manifestURL, Account committee,
            Account... signingAccounts) throws Throwable {

        checkSigningAccountPresent(signingAccounts);
        byte[] nefBytes = Files.readBytes(new File(nefURL.toURI()));
        byte[] manifestBytes = Files.readBytes(new File(manifestURL.toURI()));

        Transaction tx = new ContractManagement(neow3j)
                .invokeFunction("deploy", byteArray(nefBytes), byteArray(manifestBytes))
                .signers(AccountSigner.calledByEntry(committee))
                .getUnsignedTransaction();

        Witness multiSigWitness = createMultiSigWitness(tx.getHashData(), committee, signingAccounts);
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        long checksum = NefFile.getCheckSumAsInteger(NefFile.computeChecksumFromBytes(nefBytes));
        Hash160 contractHash = calcContractHash(committee.getScriptHash(), checksum, "NameService");

        neow3j.setNNSResolver(contractHash);
    }

    private static Witness createMultiSigWitness(byte[] hashData, Account multiSigAccount, Account... signingAccounts) {
        List<Sign.SignatureData> signatureDataList = new ArrayList<>();
        for (Account signingAccount : signingAccounts) {
            Sign.SignatureData signatureData = Sign.signMessage(hashData, signingAccount.getECKeyPair());
            signatureDataList.add(signatureData);
        }
        return Witness.createMultiSigWitness(signatureDataList, multiSigAccount.getVerificationScript());
    }

    /**
     * Adds a new root to the NNS contract.
     *
     * @param neow3j          the neow3j instance.
     * @param nnsRoot         the root.
     * @param committee       the committee account that is allowed to add a new root.
     * @param signingAccounts the signing accounts for the committee multi-sig account.
     * @throws Throwable if something goes wrong when adding the root.
     */
    public static void addNNSRoot(Neow3j neow3j, NNSName.NNSRoot nnsRoot, Account committee, Account... signingAccounts)
            throws Throwable {

        checkSigningAccountPresent(signingAccounts);
        Transaction tx = new NeoNameService(neow3j).addRoot(nnsRoot)
                .signers(AccountSigner.calledByEntry(committee))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(tx.getHashData(), committee, signingAccounts);
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    private static void checkSigningAccountPresent(Account[] signingAccounts) {
        if (signingAccounts.length == 0) {
            throw new IllegalArgumentException("No signing account provided.");
        }
    }

}
