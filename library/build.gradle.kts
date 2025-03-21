import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.plugins.signing.Sign

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.9.10"
}

val getVersionName = {
    "1.0.0"
}

// Function to read properties from local.properties file
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val properties = Properties()
    val localProperties = file("${project.rootDir}/local.properties")
    if (localProperties.exists()) {
        properties.load(FileInputStream(localProperties))
    }
    return properties.getProperty(key, defaultValue)
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

// Generate Javadoc
tasks.register("generateJavadoc", Javadoc::class) {
    description = "Generates Javadoc for the project"
    group = "documentation"
    
    // Use a simpler approach for source files
    source(android.sourceSets.getByName("main").java.srcDirs)
    
    classpath += project.files(android.bootClasspath)
    android.libraryVariants.forEach { variant ->
        if (variant.name == "release") {
            classpath += variant.javaCompileProvider.get().classpath
        }
    }
    
    // Exclude generated files
    exclude("**/R.java", "**/BuildConfig.java")
    
    // Javadoc options
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        addStringOption("encoding", "UTF-8")
        addStringOption("charSet", "UTF-8")
        links("https://docs.oracle.com/javase/8/docs/api/")
        links("https://developer.android.com/reference/")
        // Set failOnError through options
        isFailOnError = false
    }
}

// Task to generate Javadoc and sources JARs
tasks.register<Jar>("javadocJar") {
    description = "Creates a JAR with the Javadoc for the project"
    group = "documentation"
    
    dependsOn("generateJavadoc")
    archiveClassifier.set("javadoc")
    from(tasks.named("generateJavadoc"))
}

tasks.register<Jar>("sourcesJar") {
    description = "Creates a JAR with the source code of the project"
    group = "documentation"
    
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

// Task to publish to Maven Central
tasks.register("publishToMavenCentral") {
    description = "Publishes the library to Maven Central repository"
    group = "publishing"

    dependsOn("assembleRelease")
    dependsOn("javadocJar")
    dependsOn("sourcesJar")
    dependsOn("publishReleasePublicationToSonatypeRepository")

    doLast {
        println("Library successfully published to Maven Central")
        println("Group: com.telnyx.webrtc.lib")
        println("Artifact: library")
        println("Version: ${getVersionName()}")
        println("To use this library in your project, add the following dependency:")
        println("implementation 'com.telnyx.webrtc.lib:library:${getVersionName()}'")
    }
}

// Task to prepare a zip file for manual publishing to Maven Central
tasks.register("prepareManualPublishZip") {
    description = "Prepares a zip file for manual publishing to Maven Central"
    group = "publishing"

    dependsOn("assembleRelease")
    dependsOn("javadocJar")
    dependsOn("sourcesJar")
    dependsOn("signReleasePublication")

    doLast {
        // Create the directory structure following Maven repository layout
        val groupIdPath = "com/telnyx/webrtc/lib"
        val artifactId = "library"
        val version = getVersionName()
        val publishDir = file("${rootDir}/publish")
        val mavenDir = file("${publishDir}/${groupIdPath}/${artifactId}/${version}")
        
        // Clean and create directories
        if (publishDir.exists()) {
            publishDir.deleteRecursively()
        }
        mavenDir.mkdirs()
        
        // Define file names
        val aarName = "${artifactId}-${version}"
        val pomName = "${aarName}.pom"
        val javadocName = "${aarName}-javadoc.jar"
        val sourcesName = "${aarName}-sources.jar"
        
        // Copy the AAR file
        copy {
            from("${buildDir}/outputs/aar/${artifactId}-release.aar")
            into(mavenDir)
            rename { "${aarName}.aar" }
        }
        
        // Copy the POM file
        copy {
            from("${buildDir}/publications/release/pom-default.xml")
            into(mavenDir)
            rename { pomName }
        }
        
        // Copy Javadoc JAR
        copy {
            from("${buildDir}/libs/${artifactId}-${version}-javadoc.jar")
            into(mavenDir)
            rename { javadocName }
        }
        
        // Copy Sources JAR
        copy {
            from("${buildDir}/libs/${artifactId}-${version}-sources.jar")
            into(mavenDir)
            rename { sourcesName }
        }
        
        // List of files that need to be signed
        val filesToSign = listOf(
            "${mavenDir}/${aarName}.aar",
            "${mavenDir}/${pomName}",
            "${mavenDir}/${javadocName}",
            "${mavenDir}/${sourcesName}"
        )
        
        // Sign files using GPG
        val signingKeyId = getLocalProperty("signing.keyId")
        val signingPassword = getLocalProperty("signing.password")
        
        filesToSign.forEach { filePath ->
            val file = file(filePath)
            if (file.exists()) {
                // Create signature file using GPG
                if (signingKeyId.isNotEmpty() && signingPassword.isNotEmpty()) {
                    // Use GPG with key ID and password
                    exec {
                        commandLine("gpg", "--batch", "--yes", "--passphrase", signingPassword, 
                                   "--pinentry-mode", "loopback", "-ab", file.absolutePath)
                        standardOutput = System.out
                        errorOutput = System.err
                    }
                } else {
                    // Use default GPG configuration
                    exec {
                        commandLine("gpg", "--batch", "--yes", "-ab", file.absolutePath)
                        standardOutput = System.out
                        errorOutput = System.err
                    }
                }
                
                // Verify signature was created
                val sigFile = file("${file.absolutePath}.asc")
                if (!sigFile.exists()) {
                    throw GradleException("Failed to create signature for ${file.name}")
                }
            } else {
                throw GradleException("File not found: ${file.absolutePath}")
            }
        }
        
        // Generate MD5 and SHA1 checksums for all files including signature files
        fileTree(mavenDir).forEach { file ->
            if (!file.name.endsWith(".md5") && !file.name.endsWith(".sha1")) {
                // Generate MD5
                ant.withGroovyBuilder {
                    "checksum"("file" to file.absolutePath, "algorithm" to "MD5", "fileext" to ".md5")
                }
                
                // Generate SHA1
                ant.withGroovyBuilder {
                    "checksum"("file" to file.absolutePath, "algorithm" to "SHA1", "fileext" to ".sha1")
                }
            }
        }
        
        // Create a zip file
        ant.withGroovyBuilder {
            "zip"("destfile" to "${publishDir}/maven-central-bundle.zip", "basedir" to publishDir, "includes" to "${groupIdPath}/**")
        }
        
        // Print summary of files included in the bundle
        println("\n=== Maven Central Bundle Contents ===")
        println("Bundle created at: ${publishDir}/maven-central-bundle.zip")
        println("\nFiles included:")
        
        val fileTypes = mapOf(
            ".aar" to "AAR Library",
            ".pom" to "POM File",
            "-javadoc.jar" to "Javadoc JAR",
            "-sources.jar" to "Sources JAR",
            ".asc" to "PGP Signature",
            ".md5" to "MD5 Checksum",
            ".sha1" to "SHA1 Checksum"
        )
        
        fileTree(mavenDir).forEach { file ->
            val fileType = fileTypes.entries.find { file.name.endsWith(it.key) }?.value ?: "Unknown"
            println("- ${file.name} (${fileType})")
        }
        
        println("\nTo publish to Maven Central:")
        println("1. Go to https://central.sonatype.org/")
        println("2. Click on 'Publish Component'")
        println("3. Enter deployment name: com.telnyx.webrtc.lib:library:${version}")
        println("4. Upload the zip file: ${publishDir}/maven-central-bundle.zip")
    }
}

// Fix task dependencies for signing
tasks.withType<Sign>().configureEach {
    // Ensure bundleReleaseAar task runs before signing
    dependsOn("bundleReleaseAar")
}

// Maven publishing configuration
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.telnyx.webrtc.lib"
            artifactId = "library"
            version = getVersionName()

            // Use the release AAR file
            artifact("$buildDir/outputs/aar/library-release.aar")
            
            // Add sources and javadoc artifacts
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])

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
        
        // Maven Central repository using Sonatype OSSRH
        maven {
            name = "sonatype"
            // Use the newer s01 URLs as recommended by Sonatype
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (getVersionName().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = getLocalProperty("ossrhUsername")
                password = getLocalProperty("ossrhPassword")
            }
        }
    }
}

// Signing configuration for Maven Central
signing {
    // Only sign release builds
    setRequired({ gradle.taskGraph.hasTask("publishReleasePublicationToSonatypeRepository") })
    
    val signingKeyId = getLocalProperty("signing.keyId")
    val signingKey = getLocalProperty("signing.key").replace("\\n", "\n")
    val signingPassword = getLocalProperty("signing.password")

    if (signingKeyId.isNotEmpty() && signingKey.isNotEmpty() && signingPassword.isNotEmpty()) {
        // Use in-memory PGP keys if provided in local.properties
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    } else {
        // Fallback to using gpg command line tool
        useGpgCmd()
    }
    
    // Sign all publications
    sign(publishing.publications["release"])
}

// Make sure signing happens before publishing
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

// Make sure Javadoc and sources JARs are generated before signing
tasks.withType<Sign>().configureEach {
    dependsOn(tasks.named("javadocJar"))
    dependsOn(tasks.named("sourcesJar"))
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

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.slf4j)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}