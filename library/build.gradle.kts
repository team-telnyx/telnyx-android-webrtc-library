plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

val getVersionName = {
    "1.0.0"
}

// Function to read properties from local.properties file
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val properties = java.util.Properties()
    val localProperties = file("${project.rootDir}/local.properties")
    if (localProperties.exists()) {
        properties.load(java.io.FileInputStream(localProperties))
    }
    return properties.getProperty(key, defaultValue)
}

// Maven publishing configuration
publishing {
    publications {
        create<MavenPublication>("release") {
            group = "com.telnyx.webrtc.lib"
            artifactId = "library"
            version = getVersionName()

            // Use the release AAR file
            artifact("$buildDir/outputs/aar/library-release.aar")

            // Add POM information required by Maven Central
            pom {
                name.set("Telnyx WebRTC Android Library")
                description.set("Android WebRTC library for Telnyx services")
                url.set("https://github.com/team-telnyx/telnyx-android-webrtc-library")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("telnyx")
                        name.set("Telnyx")
                        email.set("support@telnyx.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/team-telnyx/telnyx-android-webrtc-library.git")
                    developerConnection.set("scm:git:ssh://github.com:team-telnyx/telnyx-android-webrtc-library.git")
                    url.set("https://github.com/team-telnyx/telnyx-android-webrtc-library")
                }
            }
        }
    }

    repositories {
        // Local repository (existing)
        maven {
            name = "localRepo"
            url = uri("$buildDir/repo")
        }
        
        // Maven Central repository (new)
        maven {
            name = "mavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            
            credentials {
                username = getLocalProperty("ossrhUsername")
                password = getLocalProperty("ossrhPassword")
            }
        }
    }
}

// Signing configuration for Maven Central
signing {
    val signingKeyId = getLocalProperty("signing.keyId")
    val signingKey = getLocalProperty("signing.key")
    val signingPassword = getLocalProperty("signing.password")
    
    if (signingKeyId.isNotEmpty() && signingKey.isNotEmpty() && signingPassword.isNotEmpty()) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["release"])
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

    from(file("$buildDirPath/library-release.aar"))
    into(outputDir)
    rename { outputFileName }
}

// Task to publish to Maven Central
tasks.register("publishToMavenCentral") {
    description = "Publishes the library to Maven Central repository"
    group = "publishing"
    
    dependsOn("assembleRelease")
    dependsOn("publishReleasePublicationToMavenCentralRepository")
    
    doLast {
        println("Library successfully published to Maven Central")
        println("Group: com.telnyx.webrtc.lib")
        println("Artifact: library")
        println("Version: ${getVersionName()}")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.slf4j)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}