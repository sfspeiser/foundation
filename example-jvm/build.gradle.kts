val artifactGroup: String by project
val foundationVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationRuntimeVersion: String by project
val javaFakerVersion: String by project

plugins {
    idea
}

group = "$artifactGroup.example-jvm"
version = foundationVersion

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    // ------- Dependencies for development
    implementation(kotlin("stdlib"))
    implementation(project(":foundation"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationRuntimeVersion")
    compile("com.github.javafaker:javafaker:$javaFakerVersion")

    kapt(project(":foundation-processor"))
    kaptTest(project(":foundation-processor"))

    // ------- Dependencies for testing with published artifact
     implementation(kotlin("stdlib"))
     implementation("com.github.nhat-phan.foundation:foundation-jvm:$foundationVersion")
     compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationRuntimeVersion")

     kapt("com.github.nhat-phan.foundation:foundation-processor:$foundationVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

idea {
    module {
        sourceDirs = sourceDirs + files(
            "build/generated/source/kapt/main",
            "build/generated/source/kaptKotlin/main"
        )

        testSourceDirs = testSourceDirs + files(
            "build/generated/source/kapt/test",
            "build/generated/source/kaptKotlin/test"
        )

        generatedSourceDirs = generatedSourceDirs + files(
            "build/generated/source/kapt/main",
            "build/generated/source/kaptKotlin/main",
            "build/generated/source/kapt/test",
            "build/generated/source/kaptKotlin/test"
        )
    }
}
