plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    id("io.quarkus") version "3.17.4"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")

    // JAX-RS and REST
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")

    // Panache with Kotlin support
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")

    // S3 / MinIO for file storage
    implementation(platform("software.amazon.awssdk:bom:2.28.11"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:url-connection-client")

    // Google Cloud Storage
    implementation(platform("com.google.cloud:libraries-bom:26.32.0"))
    implementation("com.google.cloud:google-cloud-storage")

    // OpenAPI and Swagger UI
    implementation("io.quarkus:quarkus-smallrye-openapi")

    // JWT Authentication
    implementation("io.quarkus:quarkus-smallrye-jwt")

    // Reactive routes (for route filters)
    implementation("io.quarkus:quarkus-reactive-routes")

    // Revet IAM
    implementation("com.revethq:revet-core:0.1.0")
    implementation("com.revethq.iam:revet-user:0.1.13")
    implementation("com.revethq.iam:revet-user-persistence-runtime:0.1.13")
    implementation("com.revethq.iam:revet-permission:0.1.13")
    implementation("com.revethq.iam:revet-user-web:0.1.13")
    implementation("com.revethq.iam:revet-permission-persistence-runtime:0.1.13")
    implementation("com.revethq.iam:revet-permission-web:0.1.13")
    implementation("com.revethq.iam:revet-scim:0.1.13")

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.mockk:mockk:1.13.13")
}

group = "com.ndptc.kala"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.ws.rs.ext.Provider")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        javaParameters.set(true)
    }
}
