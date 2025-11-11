import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.redis.plugin"
version = "1.0.4"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("com.intellij.java"))
}

dependencies {
    // Redis Java client
    implementation("redis.clients:jedis:6.0.0-beta2")
    
    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.mockito:mockito-core:5.3.1")
}

// Set JVM compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    // Set the JVM compatibility for Kotlin
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    // Test with JUnit 5
    test {
        useJUnitPlatform()
    }
    
    // Skip buildSearchableOptions to avoid IndexOutOfBoundsException
    buildSearchableOptions {
        enabled = false
    }

    // Prepare platform plugin for running
    runIde {
        // Increase available memory
        jvmArgs = listOf("-Xmx2048m")
    }

    // Configure plugin metadata
    patchPluginXml {
        sinceBuild.set("241") // IntelliJ 2024.1
        untilBuild.set("242.*") // Up to 2024.2
        
        // Plugin metadata
        pluginDescription.set("""
            Redis client plugin for IntelliJ IDEA that provides a comprehensive interface 
            to connect to Redis servers, execute commands, and browse Redis data.
            
            <h3>Features:</h3>
            <ul>
                <li>Redis Connection Management</li>
                <li>Command Execution with Syntax Highlighting</li>
                <li>Data Browser with Type-specific Formatters</li>
                <li>Complete Redis Command Documentation</li>
                <li>Command History and Auto-complete</li>
                <li>Results Export functionality</li>
            </ul>
        """.trimIndent())
        
        changeNotes.set("""
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Redis connection management</li>
                <li>Redis command execution</li>
                <li>Complete Redis API command list</li>
                <li>Result viewing and exporting</li>
            </ul>
        """.trimIndent())
    }

    // Add resources like icons and other non-code assets
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    // Configure signPlugin and publishPlugin tasks if you plan to publish the plugin
    // See https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        // Publish to stable channel
        channels.set(listOf("stable"))
        // Optional: configure other publishing parameters
        // toolboxEnterprise.set(true) // for Enterprise Marketplace
    }

}

