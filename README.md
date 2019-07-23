[![Build Status](https://travis-ci.org/neow3j/neow3j.svg?branch=master)](https://travis-ci.org/neow3j/neow3j)
![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/io/neow3j/core/maven-metadata.xml.svg)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/http/oss.sonatype.org/io.neow3j/core.svg)
[![codecov](https://codecov.io/gh/neow3j/neow3j/branch/master/graph/badge.svg)](https://codecov.io/gh/neow3j/neow3j)

# neow3j: A Java Library to interact with NEO nodes

<p align="center">
<img src="./images/bongo-cat-neow3j.png" alt="Bongo Cat Neow3j" width="400" height="291" />
</p>

**Neow3j** is a Java library that aims to provide an easy and reliable integration to NEO nodes/clients.

By using **neow3j**, you will happily play with NEO and end up neow'ing around like [Bongo Cat](https://knowyourmeme.com/memes/bongo-cat).

You can now focus on building Java/Android applications that use the [functions](#NEO-API-Support) provided by the NEO blockchain -- without being concerned on writing specific code to integrate with NEO nodes/clients.

**Neow3j** is an open-source project developed by the community and maintained by [AxLabs](https://axlabs.com).

## Features

* Support for NEO RPC [API version 2.10.2](https://docs.neo.org/docs/en-us/reference/rpc/latest-version/api.html)
* Observable pattern to get info about past and upcoming NEO blocks
* Asset transfers
* Contract invocations
* Passphrase-protected private keys (NEP-2)
* Wallet and Account model supporting NEP-6
* NEO-compatible Mnemonic utilities (BIP-39)
* Multisig address utilities
* Building, signing, and sending raw transactions
* Sync and async interface
* Retry on node errors
* Integration tests with the dotnet NEO VM
* Android support from API 24, which covers [~49%](https://developer.android.com/about/dashboards/) of **all active** Android devices ([~1 billion devices](https://www.youtube.com/watch?v=vWLcyFtni6U#t=2m46s))

## Upcoming Features/Enhancements

* Documentation and example Android apps
* GAS claiming
* Convenient NEP5 token contract interaction (already possible via normal contract invocation API)
* Select best seed NEO node based on some metrics (e.g., latency)

## Getting Started

Neow3j is split into multiple project modules to allow the reuse of independent functionality. 
For most use cases you will want to depend on the `contract` artifact which brings you all functionalities:

### Gradle

Java 8 & Android (min. API 24):

```groovy
compile 'io.neow3j:contract:2.0.0'
```

### Maven

Java 8 & Android (min. API 24):

```xml
<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>contract</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Examples

* Initialize Neow3j providing the JSON-RPC's endpoint of a NEO node/client:

```java
Neow3j neow3j = Neow3j.build(new HttpService("http://seed1.ngd.network:10332"));
```

* Get all blocks starting from, e.g. `2889367`, and subscribe to also get newly generated NEO blocks:

```java
neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(new BlockParameterIndex(2889367), true)
        .subscribe((blockReqResult) -> {
            System.out.println("#######################################");
            System.out.println("blockIndex: " + blockReqResult.getBlock().getIndex());
            System.out.println("hashId: " + blockReqResult.getBlock().getHash());
            System.out.println("confirmations: " + blockReqResult.getBlock().getConfirmations());
            System.out.println("transactions: " + blockReqResult.getBlock().getTransactions());
        });

```

Or, you can just subscribe to the newly generated NEO blocks:

```java
neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(BlockParameterName.LATEST, true)
        .subscribe((blockReqResult) -> {
            System.out.println("#######################################");
            System.out.println("blockIndex: " + blockReqResult.getBlock().getIndex());
            System.out.println("hashId: " + blockReqResult.getBlock().getHash());
            System.out.println("confirmations: " + blockReqResult.getBlock().getConfirmations());
            System.out.println("transactions: " + blockReqResult.getBlock().getTransactions());
        });
```

* Get the latest block index received by the NEO node:

```java
NeoBlockCount blockCountReq = neow3j.getBlockCount().send();
System.out.println(blockCountReq.getBlockIndex());
```

* Validate whether an address is a valid NEO address:

```java
NeoValidateAddress validateReq = neow3j.validateAddress("ARvMqz3hEFE4qBkHAaPNxALquNQtBbH12f").send();
System.out.println("isValid=" + validateReq.getValidation().isValid());
```

* Create a NEO-compatible key pair:

```java
ECKeyPair ecKeyPair = ECKeyPair.createEcKeyPair();
```

* Create a key pair from WIF:

```java
ECKeyPair ecKeyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF("Kx9xMQVipBYAAjSxYEoZVatdVQfhYHbMFWSYPinSgAVd1d4Qgbpf"));
```

* Generate the WIF based on a key pair:

```java
String wif = ecKeyPair.exportAsWIF();
```

* And many more key related utility methods.

* Create a new wallet with a new account:

```java
Wallet wallet = Wallet.createGenericWallet();
```

* Load a wallet stored in the NEP-6 wallet standard

```java
Wallet wallet = Wallet.fromNEP6Wallet("path/to/file");
```

* And many more wallet and account related functionalities 

* Invoke a contract

```java
Neow3j neow3j = Neow3j.build(new HttpService("https://node2.neocompiler.io"));
String contractScriptHash = "1a70eac53f5882e40dd90f55463cce31a9f72cd4";
Account acct = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
acct.updateAssetBalances(neow3j);

ContractInvocation invoc = new ContractInvocation.Builder(neow3j)
        .contractScriptHash(contractScriptHash)
        .account(acct)
        .parameter(ContractParameter.string("register"))
        .parameter(ContractParameter.array(
                ContractParameter.string("neow3j.com"),
                ContractParameter.byteArrayFromAddress(acct.getAddress())))
        .networkFee(new BigDecimal("0.1"))
        .build()
        .sign();

InvocationResult result = invoc.testInvoke();
invoc.invoke();
```

* Transfer an asset

```java
Neow3j neow3j = Neow3j.build(new HttpService("https://node2.neocompiler.io"));
Account acct = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();;
acct.updateAssetBalances(neow3j);
String toAddress = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "10", toAddress);
AssetTransfer transfer = new AssetTransfer.Builder(neow3j)
        .account(acct)
        .output(output)
        .networkFee(new BigDecimal("0.1"))
        .build()
        .sign()
        .send();
```


For more code snippets and examples, check the [neow3j-examples](https://github.com/neow3j/neow3j-examples) repository.

## Donate

Help the development of neow3j by donating to the following addresses:

| Crypto   | Address                                      |
|----------|----------------------------------------------|
| NEO      | `AHb3PPUY6a36Gd6JXCkn8j8LKtbEUr3UfZ`         |
| ETH      | `0xe85EbabD96943655e2DcaC44d3F21DC75F403B2f` |
| BTC      | `3L4br7KQ8DCJEZ77nBjJfrukWEdVRXoKiy`         |

## NEO JSON-RPC Support

[Here you can find](https://github.com/neow3j/neow3j-docs/blob/master/docs/json-rpc-supported-methods.md) a complete list of all JSON-RPC methods supported by `neow3j`.

In summary, `neow3j` supports JSON-RPC API [version 2.10.2](https://docs.neo.org/docs/en-us/reference/rpc/latest-version/api.html) and lower.

## Why "neow3j"?

This project is based on [web3j](https://web3j.io), but focusing on NEO. That's why the suffix "w3j" was added to the "neo" name, forming "neow3j".

Well... then, it was simply natural to imagine [Bongo Cat](https://knowyourmeme.com/memes/bongo-cat) playing on NEO nodes and neow'ing instead of meow'ing, don't you think? :-)

## Thanks and Credits

* [NEO Foundation](https://neo.org/team) & [NEO Global Development (NGD)](https://neo.org/team)
* This project was strongly based on [web3j](https://web3j.io),
a library originally developed by [Conor Svensson](http://conorsvensson.com), latest on [this commit](https://github.com/web3j/web3j/commit/2a259ece9736c0338fbb66b1be4c04aba0855254).
We are really thankful for it. :-)

