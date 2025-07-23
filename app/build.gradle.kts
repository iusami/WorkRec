import java.time.Duration

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.workrec"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.workrec"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Test Configuration - Enhanced Parallel Execution for CI Optimization
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            // Enable parallel test execution
            all {
                it.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                it.forkEvery = 100
                it.maxHeapSize = "2g"
                it.minHeapSize = "1g"
            }
        }
        // Configure instrumented test execution
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }
}

// Enhanced Task Configuration for Parallel Execution and CI Optimization
tasks.withType<Test> {
    // Configure optimal worker counts and memory allocation
    val availableProcessors = Runtime.getRuntime().availableProcessors()
    maxParallelForks = when {
        availableProcessors >= 8 -> 4  // High-end CI runners
        availableProcessors >= 4 -> 2  // Standard CI runners
        else -> 1                      // Minimal CI runners
    }
    
    // Fork configuration for test isolation and performance
    forkEvery = 100
    maxHeapSize = "2g"
    minHeapSize = "512m"
    
    // JVM arguments for test processes
    jvmArgs = listOf(
        "-XX:+UseG1GC",
        "-XX:+UseStringDeduplication",
        "-XX:MaxGCPauseMillis=100",
        "-Dfile.encoding=UTF-8"
    )
    
    // Test logging configuration
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
    }
    
    // Parallel execution system properties
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")
    systemProperty("junit.jupiter.execution.parallel.config.dynamic.factor", "2.0")
    
    // Performance optimization properties
    systemProperty("kotlinx.coroutines.debug", "off")
    systemProperty("kotlinx.coroutines.stacktrace.recovery", "false")
    
    // Timeout configuration
    timeout.set(Duration.ofMinutes(10))
}

// Enhanced Kotlin Compilation Optimization with Build Performance Flags
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xuse-ir",
            "-Xbackend-threads=4",
            "-Xuse-fir-lt=false",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
        // Enable incremental compilation
        incremental = true
        // Use build cache for compilation
        usePreciseJavaTracking = true
    }
}

// Android Compilation Task Optimization
tasks.withType<com.android.build.gradle.tasks.factory.AndroidUnitTest> {
    // Configure parallel execution for Android unit tests
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    forkEvery = 50
}

// KSP Optimization for both Hilt and Room
ksp {
    // Hilt configuration (migrated from KAPT)
    arg("dagger.hilt.shareTestComponents", "true")
    arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
    arg("dagger.fastInit", "enabled")
    arg("dagger.formatGeneratedSource", "disabled")
    
    // Room configuration
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.expandProjection", "true")
    arg("room.generateKotlin", "true")
    arg("room.generateKotlinCodeByDefault", "true")
    
    // CI環境では確実にRoom DAOを生成するため、インクリメンタル処理を無効化
    if (System.getenv("CI") == "true") {
        arg("room.incremental", "false")
        arg("room.forceGenerateAll", "true")
    } else {
        arg("room.incremental", "true")
    }
}

// KSP configuration - ensure all annotation processing is handled by KSP
afterEvaluate {
    // CI環境では、KSPタスクを強制的に実行
    if (System.getenv("CI") == "true") {
        tasks.withType<com.google.devtools.ksp.gradle.KspTask>().configureEach {
            outputs.upToDateWhen { false }
        }
        
        // CI環境では、リポジトリ実装クラスのコンパイルを確実に行う
        tasks.named("compileDebugKotlin") {
            dependsOn("kspDebugKotlin")
        }
        tasks.named("compileReleaseKotlin") {
            dependsOn("kspReleaseKotlin")
        }
    }
}

dependencies {
    val compose_version = "1.5.8"
    val hilt_version = "2.48"
    val room_version = "2.7.0-alpha01"
    val navigation_version = "2.7.6"

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM - すべてのCompose依存関係のバージョンを管理
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:$navigation_version")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:$hilt_version")

    // Room Database
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Date & Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Charts - 進捗可視化用
    implementation("io.github.ehsannarmani:compose-charts:0.0.13")
    
    // Core Library Desugaring - java.time API support for API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("io.mockk:mockk:1.13.8")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}