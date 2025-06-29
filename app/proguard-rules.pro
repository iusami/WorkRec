# ProGuard設定ファイル
# WorkRec アプリケーション用

# デバッグ情報を保持
-keepattributes LineNumberTable,SourceFile
-keepattributes *Annotation*

# Kotlinリフレクション対応
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Room Database設定
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt対応
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Compose対応
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**