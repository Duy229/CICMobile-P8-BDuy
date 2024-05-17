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

# keep NFC
-keep public class org.jmrtd.* {
<fields>;
<methods>;
}
-dontwarn org.jmrtd.*
-keepattributes Exceptions, Signature, InnerClasses
-keep class org.jmrtd.JMRTDSecurityProvider**
-keepclassmembers class org.jmrtd.JMRTDSecurityProvider** {
*;
}
-keep public class org.spongycastle.* {
<fields>;
<methods>;
}
-dontwarn org.spongycastle.*
-keepattributes Exceptions, Signature, InnerClasses
-keep public class net.sf.scuba.* {
<fields>;
<methods>;
*;
}
-dontwarn net.sf.scuba.*
-keepattributes Exceptions, Signature, InnerClasses
-keep public class org.ejbca.* {
<fields>;
<methods>;
}
-dontwarn org.ejbca.*
-keepattributes Exceptions, Signature, InnerClasses
-keep class org.bouncycastle.** {*;}
# MRZ
-keep public class org.slf4j.* {
<fields>;
<methods>;
}
-dontwarn org.slf4j.*
-keepattributes Exceptions, Signature, InnerClasses
-keep public class cz.adaptech.android.* {
<fields>;
<methods>;
}
-dontwarn cz.adaptech.android.*
-keepattributes Exceptions, Signature, InnerClasses
# end MRZ
-dontwarn net.sf.scuba.*
-keepattributes Exceptions, Signature, InnerClasses
-keep class net.sf.scuba.smartcards.IsoDepCardService**
-keepclassmembers class net.sf.scuba.smartcards.IsoDepCardService** {
*;
}