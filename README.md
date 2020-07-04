# Usage
Add `maven("https://dl.bintray.com/proton/Core-publishing")` in your repositories block in Gradle
```kotlin
// <root>/build.gradle.kts
allprojects{
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/proton/Core-publishing")
        // other...
    }
}
```
then add the desired library as following
```kotlin
implementation("me.proton.core:<name-of-the-lib>:<version>")
```
where `<name-of-the-lib>` reflect the one listed below, on lowercase, with `-` instead of spaces
`Util Android Shared Preferences: **0.1**` can be resolved as `me.proton.core:util-android-share-preferences:0.1`

Where there are all or some of `domain`, `data`, `presentation`, they can be imported together with the parent module.
```kotlin
implementation("~:network-domain:~")
implementation("~:network-data:~")
```
or
```kotlin
implementation("~:network:~")
```
while the first one is suitable for multi-module projects, the latter is the suggested solution for monolithic clients.

## Setup Detekt
In order to use the all-in-one Detekt configuration, you must have a `buildSrc` module.
1. create a directory called `buildSrc` in your root

2. create inside a file called `build.gradle.kts` with the following content
    ```kotlin
    plugins {
        `kotlin-dsl`
    }
    
    repositories {
        jcenter()
        maven("https://dl.bintray.com/proton/Core-publishing")
    }
    
    dependencies {
        implementation("me.proton.core:util-gradle:<latest-version>")
    }
    ```
    
3. Run a sync

4. Add `import me.proton.core.util.gradle.*` at the top of your `build.gradle.kts` and `setupDetekt()` at the bottom of it.

   If you are not using `kotlin-dsl`, create a file `dsl.gradle.kts` in your root, with the following content

   ```kotlin
   import me.proton.core.util.gradle.*
   setupDetekt()
   ```

   and then `apply from: 'dsl.gradle.kts'` as the bottom of your `build.gradle`
   
5. Setup the following stage in your `.gitlab-ci`

    ```yaml
    stages:
      - analyze
      # - others ...
    
    #####################
    detekt analysis:
      stage: analyze
      tags:
        - android
      script:
        - ./gradlew multiModuleDetekt
      artifacts:
        reports:
          codequality: config/detekt/reports/mergedReport.json
    ```

    


# Last versions

## Common

### Utils

Util Android Shared Preferences: **0.1** - _released on: Jun 10, 2020_

Util Android Work Manager: **0.1** - _released on: Jun 22, 2020_

Util Kotlin: **0.1.3** - _released on: Jul 02, 2020_

Util Gradle: **0.1.7** - _released on: Jun 23, 2020_

### Test

Test Kotlin: **0.1** - _released on: Jun 10, 2020_

Test Android: **0.1** - _released on: May 30, 2020_

Test Android Instrumented: **0.1.1** - _released on: Jun 17, 2020_

## Shared

Domain: **0.1** - _released on: Jul 03, 2020_

Presentation: **0** - _released on: ND_

Data: **0** - _released on: ND_

## Support

### Network

Network: **0.1.1** - _released on: Jul 03, 2020_

Network Domain: **0.1.1** - _released on: Jul 03, 2020_

Network Data: **0.1.1** - _released on: Jul 03, 2020_

### Crypto

Crypto: **0** - _released on: ND_

Crypto Domain: **0** - _released on: ND_

Crypto Data: **0** - _released on: ND_

## Features

### Contacts

Contacts: **0** - _released on: ND_

Contacts Domain: **0** - _released on: ND_

Contacts Presentation: **0** - _released on: ND_

Contacts Data: **0** - _released on: ND_

### Settings

Settings: **0** - _released on: ND_

Settings Domain: **0** - _released on: ND_

Settings Presentation: **0** - _released on: ND_

Settings Data: **0** - _released on: ND_

### Human Verification

Human Verification: **0** - _released on: ND_

Human Verification Domain: **0** - _released on: ND_

Human Verification Presentation: **0** - _released on: ND_

Human Verification Data: **0** - _released on: ND_
