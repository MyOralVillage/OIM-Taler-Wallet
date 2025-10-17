# Optimized ProGuard rules for Taler Wallet

# Enable aggressive optimization
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''

# OBFUSCATION IS NOW ENABLED (removed -dontobfuscate)

# Keep source file names and line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures for reflection
-keepattributes Signature
-keepattributes *Annotation*

# ============================================
# Kotlinx Serialization (REQUIRED)
# ============================================
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their serializers
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializable data classes (but allow obfuscation of private members)
-keepclassmembers @kotlinx.serialization.Serializable class * {
    public <fields>;
    public <methods>;
    *** Companion;
}

# ============================================
# Database & Models (from transaction-database)
# ============================================
# Keep public API of data models (but not everything)
-keep class net.taler.database.data_models.** {
    public <fields>;
    public <methods>;
}
-keep class net.taler.database.filter.** {
    public <fields>;
    public <methods>;
}
-keep class net.taler.database.TranxHistory {
    public *;
}

# ============================================
# Wallet - Keep public APIs only
# ============================================
# Keep only public/protected members, allow private to be obfuscated
-keep public class net.taler.wallet.** {
    public protected *;
}

# Keep native methods (JNI)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# ============================================
# Android & AndroidX
# ============================================
# Keep views for XML layouts
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep Activity/Fragment constructors
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}
-keepclassmembers class * extends android.app.Activity {
    public <init>(...);
}

# DataStore/Protobuf
-keep class androidx.datastore.*.** { *; }
-keep class com.google.protobuf.** { *; }

# ============================================
# Third-party libraries
# ============================================
# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }

# OkHttp (used by Ktor)
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ZXing QR code library
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.client.result.**

# JNA
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# Coil image loading
-keep class coil.** { *; }
-keep interface coil.** { *; }

# Compose - keep composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ============================================
# Aggressive code removal
# ============================================
# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove Kotlin intrinsics checks
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Remove debug logging from kotlinx.coroutines
-assumenosideeffects class kotlinx.coroutines.debug.internal.DebugProbesKt {
    public static *** probeCoroutine*(...);
}

# ============================================
# Warnings to ignore
# ============================================
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**

# ============================================
# R8 full mode compatibility
# ============================================
# We don't use Kotlin reflection, so we don't need to keep it
-dontwarn kotlin.reflect.jvm.internal.**

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}