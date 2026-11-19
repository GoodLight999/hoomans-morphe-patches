# Keep extension classes from colliding with obfuscated Google Phone classes.
-keep class app.morphe.extension.dialer.** { *; }
-repackageclasses 'app.morphe.extension.dialer'
