# Telnyx Android WebRTC Library
[![](https://jitpack.io/v/team-telnyx/telnyx-webrtc-android-library.svg)](https://jitpack.io/#team-telnyx/telnyx-webrtc-android-library)


🛰️ Telnyx WebRTC Android is Google's WebRTC pre-compiled library for Android by :telephone_receiver: :fire:

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
