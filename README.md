[![Build Status](https://travis-ci.org/neow3j/neow3j.svg?branch=master)](https://travis-ci.org/neow3j/neow3j)
![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/search.maven.org/maven2/io/neow3j/core/maven-metadata.xml.svg)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/http/oss.sonatype.org/io.neow3j/core.svg)
[![codecov](https://codecov.io/gh/neow3j/neow3j/branch/master/graph/badge.svg)](https://codecov.io/gh/neow3j/neow3j)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f82a724b90a94df88e11c6462f2176ca)](https://www.codacy.com/manual/gsmachado/neow3j?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=neow3j/neow3j&amp;utm_campaign=Badge_Grade)

# neow3j: A Java Library to interact with NEO nodes

<p align="center">
<img src="./images/neow3j-neo3-with-balloon.png" alt="Bongo Cat Neow3j" width="400" height="291" />
</p>

Neow3j is a Java library that aims to provide easy and reliable integration with NEO nodes.
By using neow3j, you will happily play with NEO and end up neow'ing around like [Bongo Cat](https://knowyourmeme.com/memes/bongo-cat).

You can now focus on building Java/Android applications that use the functions provided by the NEO blockchain, without being concerned about writing specific code to integrate with NEO nodes.

Visit http://docs.neow3j.io for more information on neow3j and the technical documentation.

Neow3j is an open-source project developed by the community and maintained by [AxLabs](https://axlabs.com).

## Import neow3j to your project

To get all neow3j features, add the `io.neow3j:contract` project to your dependencies. Since neow3j is split into multiple project modules, you can also depend on a subset of the functionality, e.g. if you only require certain utility methods. Check out the concept and structure of the library [here](https://neow3j.io/#/overview/concepts_and_structure).

### Releases

__Gradle__

```groovy
compile 'io.neow3j:contract:2.+'
```
__Maven__

```xml
<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>contract</artifactId>
    <version>[2.0.0,3.0.0)</version>
</dependency>
```

### Development Snapshots

If you would like to test `SNAPSHOT` versions, which preview the most recent features but are unstable and not ready for production, use the following repository. You can check out [here](https://oss.sonatype.org/content/repositories/snapshots/io/neow3j/) what snapshots are available. The '3.0.1-SNAPSHOT' as used in the example below interacts with Neo3 nodes. You can run a Neo3 node locally e.g. [here](https://github.com/AxLabs/neo3-privatenet-docker).

__Gradle__

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
    mavenCentral()
}
```

```groovy
dependencies {
    compile 'io.neow3j:contract:3.0.1-SNAPSHOT'
}
```

__Maven__

```xml
<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <name>OSS Sonatype Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>io.neow3j</groupId>
    <artifactId>contract</artifactId>
    <version>3.0.1-SNAPSHOT</version>
</dependency>
```


## Donate

Help the development of neow3j by donating to the following addresses:

| Crypto   | Address                                      |
|----------|----------------------------------------------|
| NEO      | `AHb3PPUY6a36Gd6JXCkn8j8LKtbEUr3UfZ`         |
| ETH      | `0xe85EbabD96943655e2DcaC44d3F21DC75F403B2f` |
| BTC      | `3L4br7KQ8DCJEZ77nBjJfrukWEdVRXoKiy`         |


## Thanks and Credits

* [NEO Foundation](https://neo.org/team) & [NEO Global Development (NGD)](https://neo.org/team)
* This project was strongly based on [web3j](https://web3j.io),
a library originally developed by [Conor Svensson](http://conorsvensson.com), latest on [this commit](https://github.com/web3j/web3j/commit/2a259ece9736c0338fbb66b1be4c04aba0855254).
We are really thankful for it. :-)
