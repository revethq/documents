pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    plugins {
        id("io.quarkus") version "3.17.4"
        kotlin("jvm") version "2.0.21"
        kotlin("plugin.allopen") version "2.0.21"
    }
}

rootProject.name = "kala-api"
