plugins {
    `java-library`
    id("io.freefair.aspectj.post-compile-weaving") version "8.12.1"
}


dependencies {
    implementation(project(":metrics"))
    implementation(project(":metrics-declarative"))
    aspect(project(":metrics-declarative"))
    implementation(libs.metrics.core)
    implementation(libs.slf4j.api)
    implementation(libs.aspectjrt)
    implementation(libs.aspectjweaver)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.bundles.testing)
    testImplementation(libs.logback.classic)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
