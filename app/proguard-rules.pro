# ==============================================================================
# ProGuard & R8 Keep Rules for Release Builds
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Kotlinx Serialization (@Serializable models and Navigation Routes)
# ------------------------------------------------------------------------------
-keepattributes *Annotation*,ElementValuePairs,Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializer <fields>;
}
-keepclassmembers class *$$serializer {
    *** INSTANCE;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Navigation routes marked with @Serializable
-keep @kotlinx.serialization.Serializable class com.example.newproject30.ui.navigation.** { *; }

# ------------------------------------------------------------------------------
# 2. Room Database (Entities, DAOs, Database, and TypeConverters)
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

-keep @androidx.room.Entity class * {
    <fields>;
    <init>(...);
}

-keep @androidx.room.Dao interface * {
    <methods>;
}

-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

-keep class com.example.newproject30.data.local.Converters { *; }
-keep class com.example.newproject30.data.local.entity.** { *; }
-keep class com.example.newproject30.data.local.dao.** { *; }
