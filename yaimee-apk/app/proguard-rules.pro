# เก็บ JS bridge ไว้ (กันถูกย่อชื่อ) — เผื่อเปิด minify ในอนาคต
-keepclassmembers class com.yaimee.pos.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class woyou.aidlservice.jiuiv5.** { *; }
