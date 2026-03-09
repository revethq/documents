group = "com.revethq.documents"
version = "1.0.0-SNAPSHOT"

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }
    }

    tasks.withType<Test> {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    }
}
