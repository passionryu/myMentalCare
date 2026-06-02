import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    kotlin("kapt") version "1.9.25" apply false
    id("org.springframework.boot") version "3.5.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0" apply false
}

allprojects {
    group = "com.mymentalcare"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("implementation", "com.fasterxml.jackson.core:jackson-databind:2.19.4")
        add("implementation", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.4")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")

        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        add("testImplementation", "io.mockk:mockk:1.13.5")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        add("testImplementation", "com.appmattus.fixture:fixture:1.2.0")
    }

    extensions.configure<KtlintExtension> {
        version.set("0.45.2")
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":modules:application") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

project(":modules:infrastructure:persistence") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

project(":modules:bootstrap:mymentalcare") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}
