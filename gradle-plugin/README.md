# gradle-plugin

## Development

Publish to maven local:

```
rm -rf ~/.m2/repository/io/neow3j/gradle-plugin && ./gradlew publishToMavenLocal
```

Then, on the Java project (using the plugin), add the following to the `settings.gradle`:

```
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'io.neow3j.gradle-plugin') {
                useModule("io.neow3j:gradle-plugin:${requested.version}")
            }
        }
    }
}
```

and, in the `build.gradle` file, add the following:

```
plugins {
    id 'java'
    id 'io.neow3j.gradle-plugin' version "1.0-SNAPSHOT"
}

group 'org.example'
version '1.0-SNAPSHOT'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
}

neow3jDevpack {
    className = "io.neow3j.examples.SmartContractExample"
}

dependencies {
    annotationProcessor("io.neow3j:gradle-plugin:1.0-SNAPSHOT")
}
```

where the `io.neow3j.examples.SmartContractExample` is the class which be compiled.

## Publish to Productive Gradle Plugin Repo

```
./gradlew publishPlugin
```