package io.neow3j.compiler;

import io.neow3j.compiler.sourcelookup.DirectorySourceContainer;
import io.neow3j.contract.NefFile;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.contract.NeoToken;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;

public class Main {

    static Account a = Account.fromWIF("L1eV34wPoj9weqhGijdDLtVQzUpWGHszXXpdU9dPuh2nRFFzFa7E");
    static Account committee = Account.createMultiSigAccount(
            asList(a.getECKeyPair().getPublicKey()), 1);
    static Wallet w = Wallet.withAccounts(a, committee);
    static Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"));
    static Hash160 hash = new Hash160("77d77557c7b10e45a5bc170ee494a8e1a5294c12");

    public static void main(String[] args) throws Throwable {
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:40332"));

        Account account = Account.fromWIF("L1eV34wPoj9weqhGijdDLtVQzUpWGHszXXpdU9dPuh2nRFFzFa7E");
        Wallet wallet = Wallet.withAccounts(account);

        SmartContract sc = new SmartContract(
                new Hash160("9c815729972a4a8aedcd49b51b3ec6613dc220da"), neow3j);

        NeoSendRawTransaction response = sc.invokeFunction("foo", integer(-80000))
                .wallet(wallet)
                .signers(Signer.calledByEntry(account))
                .sign()
                .send();

        Hash256 hash = response.getSendRawTransaction().getHash();
        System.out.println(hash.toString());
    }

    public static void deserialize() throws Throwable {
        SmartContract sc = new SmartContract(hash, neow);
        NeoSendRawTransaction txResult =
                sc.invokeFunction("deserialize")
                        .wallet(w)
                        .signers(Signer.calledByEntry(a.getScriptHash()))
                        .sign()
                        .send();

        Hash256 txHash = txResult.getResult().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow);
        System.out.print("\nSent tx with hash " + txHash.toString());
    }

    public static void foo() throws Throwable {
        SmartContract sc = new SmartContract(hash, neow);
        NeoSendRawTransaction txResult =
                sc.invokeFunction("foo", string("id"))
                        .wallet(w)
                        .signers(Signer.calledByEntry(a.getScriptHash()))
                        .sign()
                        .send();

        Hash256 txHash = txResult.getResult().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow);
        System.out.print("\nSent tx with hash " + txHash.toString());
    }

    public static void put() throws Throwable {
        SmartContract sc = new SmartContract(hash, neow);
        NeoSendRawTransaction txResult =
                sc.invokeFunction("put", integer(1), string("string"), byteArray("010203"))
                        .wallet(w)
                        .signers(Signer.calledByEntry(a.getScriptHash()))
                        .sign()
                        .send();

        Hash256 txHash = txResult.getResult().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow);
        System.out.print("\nSent tx with hash " + txHash.toString());
    }

    public static void get() throws Throwable {
        SmartContract sc = new SmartContract(hash, neow);
        NeoInvokeFunction response = sc.callInvokeFunction("get");

        if (response.hasError()) {
            System.out.println("Error: " + response.getError().getMessage());
            return;
        }
        if (response.getInvocationResult().getException() != null) {
            System.out.println("Exception: " + (response.getInvocationResult().getException()));
            return;
        }
    }

}
