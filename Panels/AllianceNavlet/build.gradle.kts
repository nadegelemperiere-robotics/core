val pluginNamespace = "com.bylazar.alliance"
val pluginVersion = "1.0.0"

repositories{
    maven { url = uri("https://mymaven.bylazar.com/releases") }
    maven { url = uri("https://repo.dairy.foundation/releases") }
}

dependencies {

    compileOnly("org.firstinspires.ftc:Inspection:12.0.0")
    compileOnly("org.firstinspires.ftc:Blocks:12.0.0")
    compileOnly("org.firstinspires.ftc:RobotCore:12.0.0")
    compileOnly("org.firstinspires.ftc:RobotServer:12.0.0")
    compileOnly("org.firstinspires.ftc:OnBotJava:12.0.0")
    compileOnly("org.firstinspires.ftc:Hardware:12.0.0")
    compileOnly("org.firstinspires.ftc:FtcCommon:12.0.0")
    compileOnly("org.firstinspires.ftc:Vision:12.0.0")

    implementation("com.bylazar:fullpanels:1.0.19")

}



plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("com.bylazar.svelte-assets")
}

svelteAssets {
    webAppPath = "web"
    buildDirPath = "dist"
    assetsPath = "web/plugins/$pluginNamespace"
}

android {
    namespace = pluginNamespace

    defaultConfig {
        compileSdk = 34
        minSdk = 24
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

    publishing {
        singleVariant("release") {}
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Après le bloc android {}
afterEvaluate {
    // Build the svelte web app before packaging the release assets
    tasks.matching { it.name in setOf("assembleRelease", "mergeReleaseAssets", "packageReleaseAssets", "compileReleaseKotlin") }
        .configureEach { dependsOn("copySvelteToAssetsAllianceNavlet") }
    // Debug builds reuse the committed assets, but must wait for the svelte copy if it runs in the same build
    tasks.matching { it.name == "mergeDebugAssets" }
        .configureEach { mustRunAfter("copySvelteToAssetsAllianceNavlet") }
}
