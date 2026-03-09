plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.kotlin)
    implementation(libs.kotlin.stdlib)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(libs.quarkus.jdbc.postgresql)

    // Revet IAM (for service implementations)
    implementation(libs.revet.iam.user)
    implementation(libs.revet.iam.user.persistence)
    implementation(libs.revet.iam.permission)
    implementation(libs.revet.iam.permission.persistence)
    implementation(libs.revet.iam.permission.web)

    // Revet Buckets (for DocumentStorageService, PrebuiltPolicies)
    implementation(libs.revet.buckets.core)
    implementation(libs.revet.buckets.persistence)
    implementation(libs.revet.buckets.provider.s3)
    implementation(libs.revet.buckets.provider.gcs)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.enterprise.context.RequestScoped")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        javaParameters.set(true)
    }
}
