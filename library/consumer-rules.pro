# The bundled .so files look up org.jni_zero classes from native code
# during JNI_OnLoad (and only from native code). R8 sees no Java call
# sites, so a consumer app with code shrinking enabled will strip them,
# reintroducing the UnsatisfiedLinkError / ClassNotFoundException
# startup failure this library fixes. These rules ship inside the AAR
# via consumerProguardFiles so every consumer keeps the JNI runtime
# without having to maintain rules themselves.
-keep class org.jni_zero.** { *; }
# JNI_OnLoad also looks up lib classes by name from native code
# (com/telnyx/webrtc/lib/WebRtcClassLoader in jni_zero.cc). Without this
# rule a minified consumer crashes with SIGTRAP in JNI_OnLoad.
-keep class com.telnyx.webrtc.lib.** { *; }
