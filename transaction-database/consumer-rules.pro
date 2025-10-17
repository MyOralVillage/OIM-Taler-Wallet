# Keep all data models (exposed via typealiases)
-keep class net.taler.database.data_models.** { *; }

# Keep filter classes
-keep class net.taler.database.filter.** { *; }

# Keep the main history class and its public API
-keep class net.taler.database.TranxHistory {
    public *;
}

# Keep kotlinx.serialization generated serializers
-keep class net.taler.database.data_models.**$$serializer { *; }

# Keep Companion objects for serialization
-keepclassmembers class net.taler.database.data_models.** {
    *** Companion;
}