val artifactGroup: String by project
val foundationVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationRuntimeVersion: String by project
val kotlinxSerializationRuntimeCommonVersion: String by project
//val kotlinPoetVersion = project.findProperty("javaFakerVersion").toString()
val javaFakerVersion: String by project
val javaUUIDGeneratorVersion: String by project
val kotlinxMetadataJvmVersion: String by project
plugins {
    `maven-publish`
}

group = artifactGroup
version = foundationVersion

repositories {
    jcenter()
    mavenCentral()
}


dependencies {
    implementation (kotlin ("stdlib-common"))
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:$kotlinxMetadataJvmVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinxCoroutinesVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationRuntimeCommonVersion")

    implementation (kotlin ("test-common"))
    implementation (kotlin ("test-annotations-common"))

    implementation (kotlin ("stdlib-jdk8"))
    implementation (kotlin ("reflect"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation ("com.fasterxml.uuid:java-uuid-generator:$javaUUIDGeneratorVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationRuntimeVersion")
    implementation ("com.github.javafaker:javafaker:$javaFakerVersion")
    implementation ("com.squareup:kotlinpoet:$javaFakerVersion")

    implementation (kotlin ("test"))
    implementation (kotlin ("test-junit"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("source")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
