package app.morphe.extension.dialer.callrecording;

import android.util.Log;

/**
 * Runtime diagnostics for the GoodLight999 Google Phone call-recording patch.
 *
 * The methods intentionally take no arguments so patched call sites can invoke them without
 * consuming or clobbering any registers in Google's obfuscated bytecode.
 */
public final class DebugLog {
    public static final String TAG = "GL999CallRec";

    private DebugLog() {}

    private static void info(String message) {
        Log.i(TAG, message);
    }

    public static void canRecordGateBypassed() {
        info("01 canRecord country gate bypassed");
    }

    public static void disclosureForcedToBeep() {
        info("02 disclosure type forced to BEEP_SOUND");
    }

    public static void beepGeofenceBypassed() {
        info("03 BEEP_SOUND geofence bypassed");
    }

    public static void cachedDisclosureInvalidated() {
        info("04 cached disclosure audio invalidated; silent asset will be regenerated");
    }

    public static void startingAudioCompleted() {
        info("05 starting disclosure audio COMPLETED");
    }

    public static void startingAudioFailed() {
        Log.e(TAG, "06 starting disclosure audio FAILED; inspect adjacent Google Phone throwable");
    }

    public static void recordingEngineStartRequested() {
        info("07 recording engine start requested");
    }
}
