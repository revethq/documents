plugins {
    alias(libs.plugins.quarkus)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":persistence-runtime"))

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.kotlin)
    implementation(libs.kotlin.stdlib)
    implementation(libs.quarkus.arc)

    // JAX-RS and REST
    implementation(libs.quarkus.rest)
    implementation(libs.quarkus.rest.jackson)

    // OpenAPI and Swagger UI
    implementation(libs.quarkus.smallrye.openapi)

    // Health checks (liveness, readiness, startup probes)
    implementation(libs.quarkus.smallrye.health)

    // JWT Authentication
    implementation(libs.quarkus.smallrye.jwt)

    // Reactive routes (for route filters)
    implementation(libs.quarkus.reactive.routes)

    // Docker container image
    implementation(libs.quarkus.container.image.docker)

    // CycloneDX SBOM generation
    implementation(libs.quarkus.cyclonedx)

    // Revet libraries
    implementation(libs.revet.core.web)
    implementation(libs.revet.capabilities.web)
    implementation(libs.revet.iam.user)
    implementation(libs.revet.iam.user.web)
    implementation(libs.revet.iam.permission)
    implementation(libs.revet.iam.permission.web)
    implementation(libs.revet.iam.scim)
    implementation(libs.revet.buckets.core)
    implementation(libs.revet.buckets.web)

    // GraalVM native image substitutions
    compileOnly("org.graalvm.nativeimage:svm:25.0.2")

    // Testing
    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.quarkus.junit5.mockito)
    testImplementation(libs.rest.assured)
    testImplementation(libs.mockk)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.enterprise.context.RequestScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.ws.rs.ext.Provider")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        javaParameters.set(true)
    }
}
