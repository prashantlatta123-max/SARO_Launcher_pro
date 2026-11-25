# Keep Kotlin/JNI bridge names stable: Rust resolves these symbols by JNI name.
-keep class com.sarotech.launcher.NativeBridge { *; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep application metadata needed by Kotlin tooling.
-keep class kotlin.Metadata { *; }
