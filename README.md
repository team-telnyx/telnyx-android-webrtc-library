# Telnyx Android WebRTC Library

🛰️ Telnyx WebRTC Android is Google's WebRTC pre-compiled library for Android by :telephone_receiver: :fire:

## Installation

### Maven Central (Recommended)

The library is available on Maven Central. Add the below dependency to your **module**'s `build.gradle` file:

```kotlin
dependencies {
    implementation("com.telnyx.webrtc.lib:library:1.0.0")
}
```

### JitPack (Legacy)

[![](https://jitpack.io/v/team-telnyx/telnyx-webrtc-android-library.svg)](https://jitpack.io/#team-telnyx/telnyx-webrtc-android-library)

If you prefer using JitPack, add the JitPack repository to your root `build.gradle` file:

```kotlin
allprojects {
    repositories {
        // ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("com.github.team-telnyx:telnyx-webrtc-android-library:1.0.0")
}
```

### Build

You can use this repo to build WebRTC library from our sources:

- Clone repository to your disk
- Use command `./gradlew buildAarLib`
- Library will be placed in folder `lib`

### Publishing to Maven Central

There are two ways to publish the library to Maven Central:

#### Option 1: Automated Publishing via Gradle

1. Copy `local.properties.template` to `local.properties` and fill in your Sonatype OSSRH credentials and GPG signing information:
   ```properties
   # Maven Central (Sonatype OSSRH) credentials
   ossrhUsername=your_sonatype_username
   ossrhPassword=your_sonatype_password

   # GPG Signing information for Maven Central
   signing.keyId=your_gpg_key_id_last_8_chars
   signing.password=your_gpg_key_password
   signing.key=your_gpg_secret_key_content_in_ascii_armored_format
   ```

2. Generate the library AAR file:
   ```bash
   ./gradlew buildAarLib
   ```

3. Generate Javadoc and sources JARs (required by Maven Central):
   ```bash
   ./gradlew javadocJar sourcesJar
   ```

4. Run the publishing task:
   ```bash
   ./gradlew publishToMavenCentral
   ```

5. Log in to [Sonatype OSSRH](https://s01.oss.sonatype.org/) to monitor the staged repository.

#### Option 2: Manual Publishing via Central Portal Upload

If you prefer to manually upload the bundle to Maven Central:

1. Copy `local.properties.template` to `local.properties` and fill in your GPG signing information:
   ```properties
   # GPG Signing information for Maven Central
   signing.keyId=your_gpg_key_id_last_8_chars
   signing.password=your_gpg_key_password
   signing.key=your_gpg_secret_key_content_in_ascii_armored_format
   ```

2. Make sure you have GPG installed and configured on your system. The task will use GPG to sign the files.

3. Run the task to prepare the zip file for manual publishing:
   ```bash
   ./gradlew prepareManualPublishZip
   ```

4. This will create a zip file in the `publish` directory with the proper Maven repository layout structure. The zip file contains:
   - AAR library file
   - POM file
   - Javadoc JAR
   - Sources JAR
   - PGP signature files (.asc) for all the above files
   - MD5 and SHA1 checksums for all files

5. Go to [Central Publisher Portal](https://central.sonatype.org/) and click on "Publish Component".

6. Enter a deployment name: `com.telnyx.webrtc.lib:library:1.0.0` and upload the zip file.

7. Follow the instructions on the portal to complete the publishing process.

8. If you encounter any issues with the upload, check the console output from the `prepareManualPublishZip` task for a list of all files included in the bundle and verify that all required files are present.

#### Alternative: Step-by-Step Publishing

If you prefer more control over the publishing process:

1. Build the library and stage artifacts locally:
   ```bash
   ./gradlew clean buildAarLib javadocJar sourcesJar publishReleasePublicationToLocalRepoRepository
   ```

2. Verify the staged artifacts in the `build/repo` directory.

3. Publish to Sonatype OSSRH:
   ```bash
   ./gradlew publishReleasePublicationToSonatypeRepository
   ```

4. Close and release the repository on [Sonatype OSSRH](https://s01.oss.sonatype.org/).

### Compile your own version of Google's WebRTC

Thanks to [Stream](https://getstream.io?utm_source=Github&utm_medium=Jaewoong_OSS&utm_content=Developer&utm_campaign=Github_Feb2023_Jaewoong_StreamWebRTCAndroid&utm_term=DevRelOss) everybody can make his own WebRTC library. Just follow instructions placed [here](https://getstream.io/resources/projects/webrtc/library/android/). However, it's possible that in final solution developer will have to use this new generated library, as weel, as already existed WebRTC library, pre-compiled from this same Google's sources. This will leads to conflict, because both libraries will use this same packages `org.webrtc`

To avoid this, you will have to change name of the package in Google sources. Let's say that instead of package `org.webrtc` you would like to have it as `com.example`. Here is short description how to do it:

- Follow instructions described [here](https://getstream.io/resources/projects/webrtc/library/android/)
- Go to `webrtc_android\src` and change name of package in follwoing places:
    1. in `sdk/android/src` and `sdk/android/api` change names of packages and imports. You can do it by following commands:
    ```
    find . -name "*.java" -exec sed -i 's/^package org\.webrtc;/package com.example;/g' {} +
    find . -name "*.java" -exec sed -i 's/^package org\.webrtc\.\(.*\);/package com.example.\1;/g' {} +
    find . -name "*.java" -exec sed -i 's/^import org\.webrtc\.\(.*\);/import com.example.\1;/g' {} +
    find . -type f -name "*.java" -exec sed -i 's#import static org\.webrtc\.#import static com.example.#g' {} +
    ```
    2. in `sdk/android/src` and `sdk/android/api` change names of directories from `org.webrtc` to `com.example`
    3. in file `sdk/android/BUILD.gn` change all paths from `org/webrtc` to `com/example`
    4. repeat the points 1 and 2 in folder `rtc_base/java`
    5. in file `rtc_base/BUILD.gn` change all paths from `org/webrtc` to `com/example`
    6. because we are not going to change C++ namespace: 
        - in file `sdk/android/api/com/example/VideoFrame.java` add line:
        ```
        import org.webrtc.VideoFrameBufferType;
        ```
        - in file `sdk/android/api/com/example/NetworkChangeDetector.java` add line:
        ```
        import org.webrtc.NetworkPreference;
        ```
            - in file `sdk/android/api/com/example/RtpParameters.java` add line:
        ```
        import org.webrtc.Priority
        ```
    7. now it's time to change some missing `org.webrtc` packets names in couple files. Go to each of below files, edit it, find `org.webrtc` and change to `com.example`
        ```
        sdk/android/src/java/com/example/EglBase10Impl.java
        sdk/android/src/java/com/example/CameraCapturer.java
        ```
    8. finally, it's time to change package name in C++ files, go to `sdk/android/src/jni` and run command
        ```
        find . -type f -name "*.*" -exec sed -i 's#org_webrtc_#com_telnyx_webrtc_lib_#g' {} +
        ```

Now compile sources once again by command
```
tools_webrtc/android/build_aar.py
```

Google's WebRTC pre-compiled library with package name `com.example` will be now available in file `libwebrtc.aar`
