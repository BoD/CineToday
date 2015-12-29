# Global
-dontobfuscate
-keepattributes SourceFile, LineNumberTable
-dontoptimize

# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Picasso
-dontwarn com.squareup.okhttp.**

# OkHttp (Okio)
-dontwarn okio.**

