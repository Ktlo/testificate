pluginManagement {
    plugins {
        kotlin("jvm") version "1.4.31"
        id("info.solidsoft.pitest") version "1.5.2"
    }
}

rootProject.name = "testificate"

val names = listOf(
    "intspace",
    "log",
    "zint",
    "echo-server"
)

names.forEach { name ->
    include(":$name")
    project(":$name").projectDir = file("modules/$name")
}
