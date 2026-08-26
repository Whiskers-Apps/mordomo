plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()


    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.compose.uiToolingPreviewDesktop)

            // koin
            implementation("io.insert-koin:koin-core:4.2.1")
            implementation("io.insert-koin:koin-compose:4.2.1")
            implementation("io.insert-koin:koin-compose-viewmodel:4.2.1")
            implementation("io.insert-koin:koin-compose-viewmodel-navigation:4.2.1")


            implementation(libs.kotlinx.serialization.json)

            implementation("com.github.Mono-Code-Scheme:scheme-kt:1.0.0")

            implementation("com.github.Whiskers-Apps:sniffer-kt:1.1.0")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}