plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"

}

group = "ru.artemev"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(kotlin("reflect"))
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jsoup:jsoup:1.19.1")
    implementation("org.docx4j:docx4j-core:11.5.2")
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.5.2")
//    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
//    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
