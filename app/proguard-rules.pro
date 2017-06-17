# Global
-dontobfuscate
-keepattributes SourceFile, LineNumberTable
-dontoptimize

# OkHttp (Okio)
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# Jraf Util
-dontwarn org.jraf.android.util.databinding.UtilAboutWearBinding