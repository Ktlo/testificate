kotlin.sourceSets.all {
    with(languageSettings) {
        useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
    }
}

dependencies {
    api("io.ktor:ktor-io:1.5.2")
}
