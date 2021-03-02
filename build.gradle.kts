import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import info.solidsoft.gradle.pitest.PitestPlugin

plugins {
    kotlin("jvm") apply false
    id("info.solidsoft.pitest") apply false
}

allprojects {
    group = "ktlo"
    version = "1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<KotlinPluginWrapper>()
    apply<PitestPlugin>()
    apply<JacocoPlugin>()

    configure<PitestPluginExtension> {
        junit5PluginVersion.set("0.12")
    }

    dependencies {
        "implementation"(kotlin("stdlib"))
        "testImplementation"(kotlin("test-junit5"))
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }

        withType<Test> {
            useJUnitPlatform()
        }

        withType<JacocoReport> {
            reports {
                xml.isEnabled = false
                csv.isEnabled = false
            }
        }
    }
}
