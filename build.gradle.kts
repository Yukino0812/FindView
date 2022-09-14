plugins {
    id("org.jetbrains.intellij") version "1.9.0"
    java
    kotlin("jvm") version "1.7.10"
}

group = "me.yukino.plugin"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.2")
    plugins.add("java")
}
tasks {
    patchPluginXml {
        sinceBuild.set("93.13")
        untilBuild.set("999.*")
        changeNotes.set(
            """
            
            """.trimIndent()
        )
    }
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}