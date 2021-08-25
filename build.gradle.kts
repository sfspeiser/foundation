plugins {
    // "org.jetbrains.kotlin.jvm"
    kotlin("jvm") version "1.5.20" apply false

    // "org.jetbrains.kotlin.kapt"
    kotlin("kapt") version "1.5.20" apply false

    // "kotlinx-serialization"
    id("kotlinx-serialization") version "1.5.20" apply false

    // "kotlin-multiplatform"
    id("kotlin-multiplatform") version "1.5.20" apply false
}

subprojects {
    if (name == "example-jvm") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "foundation") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "foundation-generator") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "foundation-processor") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    if (name == "generator-test") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "generator-test-contract") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
    }

    if (name == "integration-test-contract") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    if (name == "integration-test-service") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }
}