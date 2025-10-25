# Optimized ProGuard rules for Taler Wallet

# Enable aggressive optimization
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''

# OBFUSCATION IS NOW ENABLED

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
# Third-party libraries - OPTIMIZED
# ============================================

# Ktor - OPTIMIZED (only keep what's needed)
-keep class io.ktor.client.HttpClient { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.request.** { *; }
-keep class io.ktor.client.statement.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }
-keep class io.ktor.http.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**

# OkHttp (used by Ktor)
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ZXing QR code - OPTIMIZED (only keep generation, not scanning)
-keep class com.google.zxing.BarcodeFormat { *; }
-keep class com.google.zxing.EncodeHintType { *; }
-keep class com.google.zxing.MultiFormatWriter { *; }
-keep class com.google.zxing.Writer { *; }
-keep class com.google.zxing.common.BitMatrix { *; }
-keep class com.google.zxing.qrcode.QRCodeWriter { *; }
-keep class com.google.zxing.qrcode.encoder.** { *; }
-dontwarn com.google.zxing.**

# JNA - OPTIMIZED (minimal keep)
-keep class com.sun.jna.Pointer { *; }
-keep class com.sun.jna.Structure { *; }
-keep class com.sun.jna.Native { *; }
-keep class * implements com.sun.jna.Library { *; }
-dontwarn com.sun.jna.**

# Coil image loading - Let R8 handle it (Coil is R8-friendly)
# Removed: -keep class coil.** { *; }
-dontwarn coil.**

# Markwon - OPTIMIZED (only core, no tables/recycler)
-keep class io.noties.markwon.Markwon { *; }
-keep class io.noties.markwon.MarkwonConfiguration { *; }
-keep class io.noties.markwon.MarkwonPlugin { *; }
-dontwarn io.noties.markwon.**

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

# Remove Ktor logging (since we moved it to debugImplementation)
-assumenosideeffects class io.ktor.client.plugins.logging.** {
    public static *** *(...);
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