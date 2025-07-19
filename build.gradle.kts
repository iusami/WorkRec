// プロジェクトレベルのbuild.gradle.kts
buildscript {
    extra.apply {
        set("compose_version", "1.5.8")
        set("kotlin_version", "1.9.22")
        set("hilt_version", "2.48")
        set("room_version", "2.6.1")
        set("navigation_version", "2.7.6")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}

// Global build performance optimizations for all subprojects
allprojects {
    // Configure Gradle task execution optimization
    tasks.withType<JavaCompile> {
        options.isFork = true
        options.forkOptions.jvmArgs = listOf(
            "-Xmx2g",
            "-XX:+UseG1GC",
            "-XX:+UseStringDeduplication"
        )
    }
    
    // Configure Kotlin compilation for all modules
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            // Enable parallel compilation
            freeCompilerArgs += listOf(
                "-Xbackend-threads=0", // Use all available cores
                "-Xuse-ir"
            )
        }
    }
}

// Configure build cache and parallel execution at project level
gradle.projectsEvaluated {
    tasks.withType<Test> {
        // Ensure all test tasks use parallel execution
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        
        // Configure test JVM for optimal performance
        jvmArgs = listOf(
            "-XX:+UseG1GC",
            "-XX:+UseStringDeduplication",
            "-XX:MaxGCPauseMillis=100",
            "-Xmx2g"
        )
    }
}

