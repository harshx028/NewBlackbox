# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.ncertbooks.engine.** {*; }
-keep class com.ncertbooks.jni.** {*; }
-keep class mirror.** {*; }
-keep class android.** {*; }
-keep class com.android.** {*; }

-keep class com.ncertbooks.reflect.** {*; }
-keep @com.ncertbooks.reflect.annotation.BClass class * {*;}
-keep @com.ncertbooks.reflect.annotation.BClassName class * {*;}
-keep @com.ncertbooks.reflect.annotation.BClassNameNotProcess class * {*;}
-keepclasseswithmembernames class * {
    @com.ncertbooks.reflect.annotation.BField.* <methods>;
    @com.ncertbooks.reflect.annotation.BFieldNotProcess.* <methods>;
    @com.ncertbooks.reflect.annotation.BFieldSetNotProcess.* <methods>;
    @com.ncertbooks.reflect.annotation.BFieldCheckNotProcess.* <methods>;
    @com.ncertbooks.reflect.annotation.BMethod.* <methods>;
    @com.ncertbooks.reflect.annotation.BStaticField.* <methods>;
    @com.ncertbooks.reflect.annotation.BStaticMethod.* <methods>;
    @com.ncertbooks.reflect.annotation.BMethodCheckNotProcess.* <methods>;
    @com.ncertbooks.reflect.annotation.BConstructor.* <methods>;
    @com.ncertbooks.reflect.annotation.BConstructorNotProcess.* <methods>;
}