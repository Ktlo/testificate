plugins {
    application
}

application {
    mainClass.set("ktlo.app.EchoServerKt")
}

dependencies {
    implementation("io.ktor:ktor-network:1.5.2")
    implementation(project(":log"))
}
