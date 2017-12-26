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

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}