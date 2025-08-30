import org.gradle.api.plugins.JavaPluginExtension

plugins {}

subprojects {
    plugins.apply("java")
    repositories { mavenCentral() }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.withType<Test> { useJUnitPlatform() }
}
