[![Unit Tests & Code Coverage](https://github.com/neow3j/neow3j/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/neow3j/neow3j/actions/workflows/ci-cd.yml)
[![Integration Tests](https://github.com/neow3j/neow3j/actions/workflows/integration.yml/badge.svg)](https://github.com/neow3j/neow3j/actions/workflows/integration.yml)
[![codecov](https://codecov.io/gh/neow3j/neow3j/branch/main/graph/badge.svg?token=Xd0m5I7cz0)](https://codecov.io/gh/neow3j/neow3j)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ccbf3cfcfcf749a097774414362ae008)](https://www.codacy.com/gh/neow3j/neow3j/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=neow3j/neow3j&amp;utm_campaign=Badge_Grade)
![Maven Cent2Fral](https://img.shields.io/maven-central/v/io.neow3j/core?label=maven%20releases)
![Maven Central (Snapshots)](https://img.shields.io/maven-metadata/v?label=maven%20snapshots&metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fneow3j%2Fcore%2Fmaven-metadata.xml)
[![javadoc](https://javadoc.io/badge2/io.neow3j/core/javadoc.svg)](https://javadoc.io/doc/io.neow3j)

# neow3j: A Java/Kotlin/Android Development Toolkit for the Neo Blockchain

<p align="center">
<img src="./images/neow3j-neo3-with-balloon.png" alt="Bongo Cat Neow3j" width="400" height="291" />
</p>

Neow3j is a development toolkit that provides easy and reliable tools to build Neo dApps and
Smart Contracts using the Java platform (Java, Kotlin, Android). It is an open-source project
developed by the community and maintained by [AxLabs](https://axlabs.com).

**Visit [neow3j.io](https://neow3j.io) for more information and technical documentation.**

# Quickstart

Neow3j is composed of an **SDK** for dApp development and a **devpack** for smart contract
development -- which also includes a **compiler** (JVM to NeoVM). The following sections describe
how to get started with them! :rocket:

## SDK

To make use of all neow3j SDK features, add `io.neow3j:contract` to your dependencies.

__Gradle__

```groovy
implementation 'io.neow3j:contract:3.24.0'
```

__Maven__

```xml

<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>contract</artifactId>
    <version>3.24.0</version>
</dependency>
```

Releases are available for Neo Legacy and Neo N3. The example above shows the newest release of neow3j for
Neo N3. To use the latest release for Neo Legacy, use the version `2.4.0`.

## Devpack/Compiler

For smart contract development, you need the `io.neow3j:devpack` dependency. It provides all Neo-related
utilities to write your first smart contract on the Neo blockchain!

Then, add the following dependency to your project.

__Gradle__

```groovy
implementation 'io.neow3j:devpack:3.24.0'
```

__Maven__

```xml

<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>devpack</artifactId>
    <version>3.24.0</version>
</dependency>
```

> **Note:** The devpack and compiler are only available for Neo N3. Thus, Java cannot be used to
> compile smart contracts that are compatible with Neo Legacy.

## Contribute

Contributors are welcome!  

Take a look at our open [issues](https://github.com/neow3j/neow3j/issues). We’re collecting all issues in the main neow3j repository, including issues related to documentation, tutorials, and coding examples. Some issues are labelled with a level of difficulty ([beginner](https://github.com/neow3j/neow3j/labels/beginner), [intermediate](https://github.com/neow3j/neow3j/labels/intermediate), [advanced](https://github.com/neow3j/neow3j/labels/advanced)). Create new issues if you want something not on the list.

Join the [AxLabs Discord](https://discord.gg/UxQDsAzH) server to chat with us and ask questions. Here are some points that should help you get started with the code base.

- **Setup** 🔰
    - [Fork](https://github.com/neow3j/neow3j/fork) the repository and git-clone the fork.
    - Branch out from the `main` branch. PRs are merged directly into `main`.
- **Java 🍵**
    - The language version for compilation is set to Java 8 in the Gradle build file. Thus, if you don’t have a Java 8 installation, Gradle might attempt to download one automatically.
    - Of course you need an installed JDK for Gradle to be able to run. The Java version active in your environment can be higher than 8.
- **Tests 🧪**
    - Run unit tests with `./gradlew test`.
    - Run integration tests with `./gradlew integrationTest`. They require Docker for spinning up a containerized blockchain node.
- **Linting and formatting** 🧹
    - Compiler warnings are turned off in the build configuration, so when you execute a Gradle task in the command line you will not see any warnings, but your IDE might give you many.
    - There is no Gradle task for auto-formatting or validating code formatting, i.e., your formatting will go unchecked. However, there is a code style configuration in the `.idea` folder committed to the repo that you could use if you work with IntelliJ.
- **Code Structure** 🏗️
    - The code is split into multiple sub-projects based on Gradle’s multi-project feature. Though, we do not support Java 9’s module system.
    - Some sub-projects depend on others, e.g., `contract` and `devpack` depend on `core`, or`compiler` depends on `contract` and `devpack`.
    - All integration tests live in their own sub-project called `int-tests`. The reason being dependency issues if we place the tests in their respective project. 

## Who's using neow3j? :rocket:

* [Binance](https://binance.com)
* [OKEx](https://okex.com)
* [AxLabs](https://axlabs.com)
* [GrantShares](https://grantshares.io)
* [Flamingo (FUSD)](https://flamingo-1.gitbook.io/user-guide/v/master/flamingo-stablecoin-fusd)
* [NeoCompounder (cNEO)](https://neocompounder.com/)
* [Neo Blockchain Toolkit](https://marketplace.visualstudio.com/items?itemName=ngd-seattle.neo-blockchain-toolkit)
* [NekoHit](https://nekohit.com)
* [NeoCandy](https://neocandy.io)
* [Neo Playground](https://neo-playground.dev)
* [Elements](https://www.getelements.dev)
* [intellij-neo](https://github.com/intellij-neo/intellij-neo)
* Would like to be listed here? [Contact us](mailto:info@neow3j.io)
  or [open an issue](https://github.com/neow3j/neow3j/issues).

## Donate :moneybag:

Help the development of neow3j by sponsoring us using the following addresses:

| Crypto     | Address                                      |
|------------|----------------------------------------------|
| Neo N3     | `NfhQyNmMCLCKaaazL6gbvYxtkZNGVb8kRn`         |
| Neo Legacy | `AHb3PPUY6a36Gd6JXCkn8j8LKtbEUr3UfZ`         |
| ETH        | `0xe85EbabD96943655e2DcaC44d3F21DC75F403B2f` |
| BTC        | `3L4br7KQ8DCJEZ77nBjJfrukWEdVRXoKiy`         |

## Thanks and Credits :pray:

* [NEO Foundation](https://neo.org/contributors) & [NEO Global Development (NGD)](https://neo.org/contributors)
* This project was strongly based on [web3j](https://web3j.io) latest
  on [this commit](https://github.com/web3j/web3j/commit/2a259ece9736c0338fbb66b1be4c04aba0855254). We are really
  thankful for it. :smiley:
