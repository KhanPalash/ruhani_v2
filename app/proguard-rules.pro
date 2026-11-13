# R8 / resource-shrinker rules for the Ruhani release build.
# Debug builds do not run R8, so every rule here is only exercised by
# `assembleRelease`. Keep that in mind when adding a dependency.

# Keep the original file name and line numbers in stack traces, and drop the
# obfuscated source-file placeholder. The full mapping still lands in
# app/build/outputs/mapping/release/mapping.txt and should be uploaded to Play
# Console (or kept with the release) so crash reports can be de-obfuscated.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firestore reads @ServerTimestamp and @DocumentId through reflection, and uses
# the generic signatures on Kotlin collections, so the annotations and the
# Signature attribute must survive shrinking.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ---------------------------------------------------------------------------
# Firestore document models
# ---------------------------------------------------------------------------
# DocumentSnapshot#toObject() / QuerySnapshot#toObjects() instantiate these
# classes reflectively: they inspect the declared fields and pick a constructor.
# firebase-firestore ships consumer rules for its own internals but it cannot
# know about app models, so renaming or removing these breaks cloud sync with a
# "No properties to serialize" / empty-object failure that only shows up at
# runtime in a release build.
-keep class com.khanscreations.ruhani.data.model.** { *; }
-keepclassmembers class com.khanscreations.ruhani.data.model.** { *; }

# ---------------------------------------------------------------------------
# Enums resolved from a stored string
# ---------------------------------------------------------------------------
# Settings are persisted as strings and read back with Enum.valueOf() (for
# example NotificationCategory.valueOf() for the stored notification logs), so
# the generated values()/valueOf() members must not be stripped. R8 does not
# rename enum constants for exactly this reason, but the members themselves are
# still removable.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
