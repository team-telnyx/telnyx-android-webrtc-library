# Telnyx Android WebRTC Library
[![](https://jitpack.io/v/team-telnyx/telnyx-webrtc-android-library.svg)](https://jitpack.io/#team-telnyx/telnyx-webrtc-android-library)


🛰️ Telnyx WebRTC Android is Google's WebRTC pre-compiled library for Android by Telnyx :telephone_receiver: :fire:

## What is this library?

This is a pre-compiled version of Google's WebRTC library for Android, maintained by Telnyx. It serves as the foundation for the [Telnyx Android Voice SDK](https://github.com/team-telnyx/telnyx-webrtc-android).

For more detailed information about this library, please refer to our official documentation at [Telnyx Precompiled WebRTC Library Documentation](https://developers.telnyx.com/docs/voice/webrtc/android-sdk/precompiled-library).

For more information about WebRTC for Android, you can also refer to the [WebRTC-Android documentation](https://getstream.github.io/webrtc-android/).

## What this library is NOT

This is NOT the Telnyx Android Voice SDK ([https://github.com/team-telnyx/telnyx-webrtc-android](https://github.com/team-telnyx/telnyx-webrtc-android)). It is Google's WebRTC pre-compiled library for Android by Telnyx. The Telnyx Android Voice SDK is built on top of this library.

### Using the Telnyx Android Voice SDK

If you're looking to integrate Telnyx's voice calling capabilities into your Android application, you should use the [Telnyx Android Voice SDK](https://github.com/team-telnyx/telnyx-webrtc-android) instead of this library directly. The Voice SDK provides a higher-level API that makes it easy to implement voice calling features in your application.

To use the Telnyx Android Voice SDK, follow the instructions in the [SDK repository](https://github.com/team-telnyx/telnyx-webrtc-android).

## Getting Started with Telnyx

To use Telnyx services, you'll need to [sign up for a Telnyx account](https://telnyx.com/sign-up).

### Gradle

Add the below dependency to your **module**'s `build.gradle` file:

```kotlin
dependencies {
    implementation("com.telnyx.webrtc.lib:library:1.0.0")
}
```

### Build

You can use this repo to build WebRTC library from our sources:

- Clone repository to your disk
- Use command `./gradlew buildAarLib`
- Library will be placed in folder `lib`

### Documentation

The library includes Javadoc documentation that can be generated using:

```bash
./gradlew :library:generateJavadoc
```

This will generate the documentation in `library/build/docs/javadoc/`.

You can also access the latest Javadoc documentation online through our GitHub Pages site (generated automatically when the manual GitHub Action is triggered).

### Compile your own version of Google's WebRTC

You can compile your own version of the WebRTC library from Google's sources. However, it's possible that in your final solution, you will have to use this newly generated library alongside an existing WebRTC library pre-compiled from the same Google sources. This will lead to conflicts because both libraries will use the same package `org.webrtc`

To avoid this, you will have to change name of the package in Google sources. Let's say that instead of package `org.webrtc` you would like to have it as `com.example`. Here is short description how to do it:

- Follow the official WebRTC build instructions for Android from the [WebRTC development documentation](https://webrtc.github.io/webrtc-org/native-code/android/)
- Go to `webrtc_android\src` and change name of package in following places:
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
