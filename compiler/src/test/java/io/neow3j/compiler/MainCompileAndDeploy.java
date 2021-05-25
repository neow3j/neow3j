package io.neow3j.compiler;

import io.neow3j.contract.ContractManagement;
import io.neow3j.types.Hash160;
import io.neow3j.contract.NefFile;
import io.neow3j.script.ScriptReader;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainCompileAndDeploy {

    static Account a = Account.fromWIF("L1eV34wPoj9weqhGijdDLtVQzUpWGHszXXpdU9dPuh2nRFFzFa7E");
    static Account multiSigAcc = Account.createMultiSigAccount(
            Arrays.asList(a.getECKeyPair().getPublicKey()), 1);
    static Wallet w = Wallet.withAccounts(multiSigAcc, a);
    static Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"));

    public static void main(String[] args) throws Throwable {
        CompilationUnit result = compileContract(args[0]);
        writeContractToFile(result, args[1]);
        deployContract(result.getNefFile(), result.getManifest());
    }

    private static CompilationUnit compileContract(String contractName) throws IOException {
        CompilationUnit result = new Compiler().compile(contractName);
        System.out.printf("Contract '%s' compiled.\n", contractName);
        return result;
    }

    private static Hash160 deployContract(NefFile nef, ContractManifest manifest)
            throws Throwable {

        Transaction tx = new ContractManagement(neow)
                .deploy(nef, manifest)
                .wallet(w)
                .signers(Signer.global(a.getScriptHash()))
                .sign();

        NeoSendRawTransaction response = tx.send();
        System.out.printf("\nSent deployment transaction with hash: '%s'\n\n", tx.getTxId());
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow);

        NeoApplicationLog appLog = neow.getApplicationLog(tx.getTxId()).send().getApplicationLog();
        List<StackItem> stack = appLog.getExecutions().get(0).getStack().get(0).getList();
        Hash160 contractHash = new Hash160(Numeric.reverseHexString(stack.get(2).getHexString()));
        System.out.printf("Contract hash '%s'.\n\n", contractHash.toString());
        return contractHash;
    }

    private static void writeContractToFile(CompilationUnit compUnit, String outDirPath)
            throws IOException {

        File outDir = new File(outDirPath);
        if (!outDir.exists() && !outDir.mkdir()) {
            throw new IllegalArgumentException("Directory " + outDirPath + " does not exist and "
                    + "cannot be created.");
        }
        try (FileOutputStream s = new FileOutputStream(outDirPath + "contract.nef")) {
            s.write(compUnit.getNefFile().toArray());
        }
        try (FileOutputStream s = new FileOutputStream(outDirPath
                + "contract.manifest.json")) {
            ObjectMapperFactory.getObjectMapper().writeValue(s, compUnit.getManifest());
        }
        try (FileOutputStream s = new FileOutputStream(outDirPath + "contract.debug.json")) {
            ObjectMapperFactory.getObjectMapper().writeValue(s, compUnit.getDebugInfo());
        }
        try (ZipOutputStream s = new ZipOutputStream(
                new FileOutputStream(outDirPath + "contract.nefdbgnfo"))) {
            s.putNextEntry(new ZipEntry("contract.debug.json"));
            byte[] bytes = ObjectMapperFactory.getObjectMapper()
                    .writeValueAsBytes(compUnit.getDebugInfo());
            s.write(bytes);
            s.closeEntry();
        }
        try (FileOutputStream s = new FileOutputStream(outDirPath + "contract-script.txt")) {
            String script = ScriptReader.convertToOpCodeString(compUnit.getNefFile().getScript());
            s.write(script.getBytes());
        }
        System.out.printf("Nef, manifest and debug info written to directory at %s.\n", outDirPath);
    }

}
