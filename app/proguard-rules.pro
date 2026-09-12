# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# AdMob ProGuard rules to ensure no AdMob/Play Services classes/methods are stripped or obfuscated
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**

# Keep app data models, Room entities, and DAO classes
-keep class com.example.data.** { *; }
-keep class com.example.ocr.** { *; }
-keep class com.example.util.** { *; }

# Keep Room generated classes and database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep Moshi models and annotations
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Preserve ML Kit
-dontwarn com.google.mlkit.**

# Keep line numbers for stack traces in release crash reports
-keepattributes SourceFile,LineNumberTable
