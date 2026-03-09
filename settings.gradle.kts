pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "revet-documents"

include("core")
include("persistence-runtime")
include("web")
