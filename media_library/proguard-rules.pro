# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidEnvironment\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
-keep class kotlin.jvm.internal.DefaultConstructorMarker
-keep class com.tomy.lib.ui.view.layout.MainLinearLayout$OnKeyPressedListener
-keep class com.zzx.utils.system.SystemUtil{*;}
-dontwarn android.os.SystemProperties
-keep class android.os.SystemProperties{*;}
-keep class com.zzx.media.recorder.IRecorder$State

-keepclassmembers class android.media.MediaMetadataRetriever {
    public android.graphics.Bitmap getScaledFrameAtTime(long,int,int,int);
}

-keepclassmembers class ** {
    public void onEvent*(**);
}