# Keep domain models & kotlinx.serialization serializers intact under R8
-keep class com.creditauto.app.domain.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.creditauto.app.domain.**$$serializer { *; }
-keepclassmembers class com.creditauto.app.domain.** {
    *** Companion;
}
-keepclasseswithmembers class com.creditauto.app.domain.** {
    kotlinx.serialization.KSerializer serializer(...);
}
