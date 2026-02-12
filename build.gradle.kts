import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("com.github.bjornvester.xjc") version "1.9.0"
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://build.shibboleth.net/maven/releases/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencyCheck {
    suppressionFiles.add("cpg-suppressions.xml")
}

dependencies {

    implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.10")

    implementation("org.springframework.ws:spring-ws-security:4.1.1") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
    }
    implementation("com.microsoft.azure:applicationinsights-web:3.7.6")

    api("software.amazon.awssdk:s3")
    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5") {
        exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.4")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.19.2")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.3.2")
    //runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")

    testImplementation("org.springframework.ws:spring-ws-test:4.1.1")
}

xjc {
    xsdDir.set(layout.projectDirectory.dir("src/main/resources/xsd"))
    outputJavaDir.set(layout.buildDirectory.dir("generated/sources/xjc"))
    useJakarta.set(true)
}

tasks {
    register<Copy>("copyAgentConfig") {
        description = "Copy applicationinsights.json to build.lib so App Insights config is applied correctly"
        from("applicationinsights.json")
        into("$buildDir/libs")
    }
}

tasks.named("assemble") {
    dependsOn("copyAgentConfig")
}

tasks.named("compileKotlin") {
    dependsOn("xjc")
}

tasks.matching { it.name == "runKtlintCheckOverMainSourceSet" }.configureEach {
    dependsOn("xjc")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}
