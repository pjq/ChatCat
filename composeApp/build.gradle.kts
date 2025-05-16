import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.1.21"
//    id("dev.icerock.mobile.multiplatform-resources") version "0.23.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm("desktop")
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting
        // val iosMain by getting
        
        commonMain.dependencies {
    implementation("androidx.compose.ui:ui:1.0.0")
    implementation("androidx.compose.material:material:1.0.0")
    implementation("androidx.compose.ui:ui-tooling:1.0.0")
    implementation("androidx.compose.foundation:foundation:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Internationalization
//            implementation("dev.icerock.moko:resources:0.23.0")
//            implementation("dev.icerock.moko:resources-compose:0.23.0")
            
            // Networking
            implementation("io.ktor:ktor-client-core:3.1.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
            
            // Additional Ktor dependencies needed for OpenAI client
            implementation("io.ktor:ktor-client-logging:3.1.3")
            implementation("io.ktor:ktor-client-auth:3.1.3")
            // HttpTimeout is part of these modules
            implementation("io.ktor:ktor-client-cio:3.1.3")
            // Add explicit dependency for HttpTimeout
            implementation("io.ktor:ktor-client-core:3.1.3")
            
            // OpenAI Kotlin client
             implementation("com.aallam.openai:openai-client:4.0.1")
            // implementation("io.ktor:ktor-client-core-jvm:3.1.3")

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            
            // Storage - use the same version for all platforms
            implementation("com.russhwolf:multiplatform-settings:1.1.1")
            implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            
            // Date/Time
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            
            // Navigation
            implementation("moe.tlaster:precompose:1.5.10")
            implementation("moe.tlaster:precompose-viewmodel:1.5.10")
            
            // Markdown
            implementation("com.mikepenz:multiplatform-markdown-renderer:0.34.0")
            // Material 3 defaults for Markdown
//            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.10.0")
        }
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // Platform-specific Ktor HTTP client engine
            implementation("io.ktor:ktor-client-android:3.1.3")
            // OpenAI client engine - use implementation instead of runtimeOnly
            implementation("io.ktor:ktor-client-okhttp:3.1.3")
            // Add HttpTimeout support explicitly for Android
            implementation("io.ktor:ktor-client-core-jvm:3.1.3")
        }
        
        iosMain.dependencies {
            // Platform-specific Ktor HTTP client engine
            implementation("io.ktor:ktor-client-darwin:3.1.3")
            // OpenAI client engine - use implementation instead of runtimeOnly
            implementation("io.ktor:ktor-client-darwin:3.1.3")
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            // Platform-specific Ktor HTTP client engine
            implementation("io.ktor:ktor-client-java:3.1.3")
            // OpenAI client engine - use implementation instead of runtimeOnly
            implementation("io.ktor:ktor-client-okhttp:3.1.3")
        }
        
        wasmJsMain.dependencies {
            // Platform-specific Ktor HTTP client engine
            implementation("io.ktor:ktor-client-js:3.1.3")
            // OpenAI client engine - use implementation instead of runtimeOnly
            implementation("io.ktor:ktor-client-js:3.1.3")
            
        }
    }
}

android {
    namespace = "me.pjq.chatcat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.pjq.chatcat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "me.pjq.chatcat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "me.pjq.chatcat"
            packageVersion = "1.0.0"
        }
    }
}

//multiplatformResources {
//    multiplatformResourcesPackage = "me.pjq.chatcat.resources"
//    disableStaticFrameworkWarning = true
//}
