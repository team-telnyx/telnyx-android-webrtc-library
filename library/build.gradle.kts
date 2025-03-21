plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

val getVersionName = {
    "1.0.0"
}

publishing {
    publications {
        create<MavenPublication>("release") {
            group = "com.telnyx.webrtc.lib"
            artifactId = "library"
            version = getVersionName()

            artifact(file("build/outputs/aar/app-debug.aar"))
        }
    }

    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }
}

android {
    namespace = "com.telnyx.webrtc.lib"
    compileSdk = 34

    lint {
        abortOnError = false
    }

    defaultConfig {
        minSdk = 23
        version = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("webrtc_libs")
        }
    }
}

// main task to build library
tasks.register<Copy>("buildAarLib") {
    dependsOn("assembleRelease")

    val buildDirPath = "$buildDir/outputs/aar"
    val outputDir = file("$rootDir/lib")
    val outputFileName = "telnyx-webrtc-android-library-release-${getVersionName()}.aar"

    from(file("$buildDirPath/app-release.aar"))
    into(outputDir)
    rename { outputFileName }
}

// Task to generate Javadoc
tasks.register<Javadoc>("generateJavadoc") {
    description = "Generates Javadoc for the WebRTC Android Library"
    title = "Telnyx WebRTC Android Library Documentation"
    
    source = android.sourceSets.getByName("main").java.srcDirs
    classpath += project.files(android.bootClasspath)
    android.libraryVariants.forEach { variant ->
        if (variant.name == "release") {
            classpath += variant.javaCompileProvider.get().classpath
        }
    }
    
    options {
        windowTitle = "Telnyx WebRTC Android Library"
        header = "Telnyx WebRTC Android Library"
        bottom = "Copyright © ${java.time.Year.now()} Telnyx. All rights reserved."
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PUBLIC
    }
    
    // Exclude generated files
    exclude("**/BuildConfig.java", "**/R.java")
    
    // Output directory
    setDestinationDir(file("$buildDir/docs/javadoc"))
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.slf4j)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}