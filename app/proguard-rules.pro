# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection capabilities within your code, typically used in serialization libraries,
# you need to specify that reflection metadata should be kept.
-keepattributes Signature

# For using GSON with ProGuard, if you are planning to use it.
-keep class com.google.gson.annotations.** { *; }

# If you're using retrofit, you might need this.
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
