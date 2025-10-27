# Add project specific Proguard rules here.
# You can find more information about Proguard in the official documentation:
# https://www.guardsquare.com/en/products/proguard

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
