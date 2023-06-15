import java.util.Locale

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

if (!file(".git").exists()) {
    val errorText = """
        
        =====================[ ERROR ]=====================
         The Paper project directory is not a properly cloned Git repository.
         
         In order to build Paper from source you must clone
         the Paper repository using Git, not download a code
         zip from GitHub.
         
         Built Paper jars are available for download at
         https://papermc.io/downloads/paper
         
         See https://github.com/PaperMC/Paper/blob/master/CONTRIBUTING.md
         for further information on building and modifying Paper.
        ===================================================
    """.trimIndent()
    error(errorText)
}

rootProject.name = "paper"

for (name in listOf("Paper-API", "Paper-Server", "Paper-MojangAPI")) {
    val projName = name.toLowerCase(Locale.ENGLISH)
    include(projName)
    val projectDescriptor = findProject(":$projName")!!
    projectDescriptor.projectDir = file(name)
    loadVersionCatalogFrom(projectDescriptor)
}

val testPlugin = file("test-plugin.settings.gradle.kts")
if (testPlugin.exists()) {
    apply(from = testPlugin)
} else {
    testPlugin.writeText("// Uncomment to enable the test plugin module\n//include(\":test-plugin\")\n")
}

loadVersionCatalogFrom(rootProject)

fun loadVersionCatalogFrom(projectDescriptor: ProjectDescriptor) {
    val file = projectDescriptor.projectDir.resolve("gradle/libs.versions.toml")
    if (!file.exists()) {
        return
    }
    dependencyResolutionManagement {
        versionCatalogs {
            val action = Action<VersionCatalogBuilder> {
                file.inputStream().use {
                    org.gradle.api.internal.catalog.parser.TomlCatalogFileParser.parse(it, this)
                }
            }
            if (findByName("libs") != null) {
                named("libs", action)
            } else {
                create("libs", action)
            }
        }
    }
}
