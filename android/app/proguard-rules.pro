# ── Escape Call ProGuard Rules ──

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Keep the Config constants
-keep class com.escapecall.Config { *; }
