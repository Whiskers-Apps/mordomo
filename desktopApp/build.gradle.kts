import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlin.serialization)
}

dependencies {

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    implementation(project(":shared"))

    implementation("io.insert-koin:koin-core:4.2.1")


}

compose.desktop {
    application {
        mainClass = "org.whiskersapps.mordomo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "mordomo"
            packageVersion = "1.0.0"
        }
    }
}