import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val mockHojeProp = localProperties.getProperty("MOCK_HOJE_DEV", "").replace("\"", "")
val mockHojeDev = "\"$mockHojeProp\""

val roomVersion = providers.gradleProperty("roomVersion").get()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.6"
    id("jacoco")
    id("org.sonarqube")
}

android {
    namespace = "com.example.chama"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chama"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "CHAMA (DEV)")
            buildConfigField("String", "DATA_CORTE_MOCK", mockHojeDev)
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "CHAMA")
            buildConfigField("String", "DATA_CORTE_MOCK", "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material3:material3:1.3.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:${roomVersion}")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.vanniktech:android-image-cropper:4.6.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testes Unitários Locais (JVM)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.work:work-testing:2.9.1")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoCombinedReport") {
    dependsOn("testDevDebugUnitTest", "connectedDevDebugAndroidTest")

    group = "Reporting"
    description = "Gera o relatório de cobertura unificado (Unitários + Instrumentados)"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/ComposableSingletons*",
        "**/*_Factory*",
        "**/*_Impl*",
        "**/*_ViewBinding*",
        "**/ui/screens/**",
        "**/ui/theme/**",
        "**/ui/components/**",
        "**/MainActivity*.*",
        "**/ChamaApplication*.*",
        "**/PdfPresencaGenerator*.*"
    )

    val buildDir = project.layout.buildDirectory.get().asFile

    // Caminho real identificado pelo comando 'find'
    val kotlinClassesDir = File(buildDir, "intermediates/built_in_kotlinc/devDebug/compileDevDebugKotlin/classes")
    val javaClassesDir = File(buildDir, "intermediates/javac/devDebug/compileDevDebugJavaWithJavac/classes")

    val classTrees = listOf(kotlinClassesDir, javaClassesDir)
        .filter { it.exists() }
        .map { dir ->
            fileTree(dir) {
                exclude(fileFilter)
            }
        }

    classDirectories.setFrom(files(classTrees))

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))

    // Filtra apenas arquivos binários de execução que realmente existem no disco
    val execLocal = File(buildDir, "outputs/unit_test_code_coverage/devDebugUnitTest/testDevDebugUnitTest.exec")
    val execLocalAlt = File(buildDir, "jacoco/testDevDebugUnitTest.exec")
    val execConnected = fileTree(File(buildDir, "outputs/code_coverage/devDebugAndroidTest/connected")) {
        include("**/*.ec")
    }

    val validExecData = mutableListOf<Any>()
    if (execLocal.exists()) validExecData.add(execLocal)
    else if (execLocalAlt.exists()) validExecData.add(execLocalAlt)
    validExecData.add(execConnected)

    executionData.setFrom(files(validExecData))
}

sonar {
    properties {
        property("sonar.projectKey", "vinicamargo_chama")
        property("sonar.organization", "vinicamargo")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                "${layout.buildDirectory.get()}/reports/coverage/test/dev/debug/report.xml",
                "${layout.buildDirectory.get()}/reports/coverage/test/devDebug/report.xml",
                "${layout.buildDirectory.get()}/reports/jacoco/createDevDebugUnitTestCoverageReport/createDevDebugUnitTestCoverageReport.xml"
            ).joinToString(",")
        )

        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/MainActivity*.*",
                "**/ChamaApplication*.*",
                "**/theme/**",
                "**/components/**",
                "**/ui/screens/**",
                "**/navigation/**",
                "**/PdfPresencaGenerator*.*",
                "**/NotificacaoAgendador*.*"
            ).joinToString(",")
        )
    }
}