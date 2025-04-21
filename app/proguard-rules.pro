-verbose
-optimizationpasses 5

-dontwarn org.conscrypt.**
-dontwarn kotlinx.serialization.**

# Keep DataStore fields
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
   <fields>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-keep class com.dergoogler.mmrl.webui.model.ModId { *; }
-keep class com.dergoogler.mmrl.webui.interfaces.** { *; }
-keep class com.dergoogler.mmrl.webuix.ui.activity.webui.interfaces.KernelSUInterface { *; }