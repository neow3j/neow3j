[![neow3j Actions Status](https://github.com/neow3j/neow3j/workflows/neow3j-ci-cd/badge.svg)](https://github.com/neow3j/neow3j/actions)
![Maven Central](https://img.shields.io/maven-central/v/io.neow3j/core?label=maven%2Fgradle)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/http/oss.sonatype.org/io.neow3j/core.svg)
[![javadoc](https://javadoc.io/badge2/io.neow3j/core/javadoc.svg)](https://javadoc.io/doc/io.neow3j)
[![codecov](https://codecov.io/gh/neow3j/neow3j/branch/main/graph/badge.svg?token=Xd0m5I7cz0)](https://codecov.io/gh/neow3j/neow3j)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ccbf3cfcfcf749a097774414362ae008)](https://www.codacy.com/gh/neow3j/neow3j/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=neow3j/neow3j&amp;utm_campaign=Badge_Grade)

# neow3j: A Java/Kotlin/Android Development Toolkit for the Neo Blockchain

<p align="center">
<img src="./images/neow3j-neo3-with-balloon.png" alt="Bongo Cat Neow3j" width="400" height="291" />
</p>

Neow3j is a development toolkit that provides easy and reliable tools to build Neo dApps and
Smart Contracts using the Java platform (Java, Kotlin, Android). It is an open-source project
developed by the community and maintained by [AxLabs](https://axlabs.com).

**Visit [neow3j.io](https://neow3j.io) for more information and technical documentation.**

<table>
  <tr>
    <td>
      <a href="https://marketplace.digitalocean.com/apps/1clicknode-for-neo">
      <picture>
        <source media="(prefers-color-scheme: dark)" srcset="https://cdn.axlabs.net/1clicknode-logos/1clicknode-logo-for-dark.png"
          width="200">
        <source media="(prefers-color-scheme: light)" srcset="https://cdn.axlabs.net/1clicknode-logos/1clicknode-logo-for-light.png"
          width="200">
        <img alt="1ClickNode Logo" src="">
      </picture> 
    </a>
    </td>
    <td>
      If you are in need of a testnet or mainnet Neo node for your dApp but don't want to spend hours on setup, check 
      out <a href="https://marketplace.digitalocean.com/apps/1clicknode-for-neo">1ClickNode</a>.
    </td>
  </tr>
</table>

# Quickstart

Neow3j is composed of an **SDK** for dApp development and a **devpack** for smart contract
development -- which also includes a **compiler** (JVM to NeoVM). The following sections describe
how to get started with them! :rocket:

## SDK

To make use of all neow3j SDK features, add `io.neow3j:contract` to your dependencies.

__Gradle__

```groovy
implementation 'io.neow3j:contract:3.23.0'
```

__Maven__

```xml

<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>contract</artifactId>
    <version>3.23.0</version>
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
implementation 'io.neow3j:devpack:3.23.0'
```

__Maven__

```xml

<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>devpack</artifactId>
    <version>3.23.0</version>
</dependency>
```

> **Note:** The devpack and compiler are only available for Neo N3. Thus, Java cannot be used to
> compile smart contracts that are compatible with Neo Legacy.

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
