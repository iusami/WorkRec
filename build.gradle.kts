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