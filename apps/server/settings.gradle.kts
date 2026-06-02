rootProject.name = "mymentalcare-server"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":modules:domain",
    ":modules:application",
    ":modules:infrastructure:persistence",
    ":modules:bootstrap:mymentalcare",
)
