kotlin {
    explicitApi()

    sourceSets.all {
        with(languageSettings) {
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
    implementation("com.amihaiemil.web:eo-yaml:5.1.9")
    implementation(kotlin("reflect"))
}
