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

    public static void startingAudioFailed(Throwable throwable) {
        Log.e(TAG, "06 starting disclosure audio FAILED", throwable);
    }

    public static void recordingEngineStartRequested() {
        info("07 recording engine start requested");
    }

    public static void featurePresenceGateBypassed() {
        info("10 CallRecordingEnabledFn forced enabled");
    }

    public static void availabilityEmergencyCall() {
        info("20 availability=false: emergency call");
    }

    public static void availabilityEmergencyCallback() {
        info("21 availability=false: emergency callback");
    }

    public static void availabilityConferenceChild() {
        info("22 availability=false: child of conference call");
    }

    public static void availabilityMultipleCalls() {
        info("23 availability=false: multiple calls in progress");
    }

    public static void availabilityConferenceCall() {
        info("24 availability=false: conference call");
    }

    public static void availabilityCdma() {
        info("25 availability=false: CDMA network");
    }

    public static void availabilityVideoCall() {
        info("26 availability=false: video call");
    }

    public static void availabilityRttCall() {
        info("27 availability=false: RTT call");
    }

    public static void availabilityFiCall() {
        info("28 availability=false: Fi call");
    }

    public static void availabilityFeatureMissing() {
        info("29 availability=false: call-recording feature not present");
    }

    public static void availabilityCanRecordFalse() {
        info("30 availability=false: CanRecord returned false");
    }

    public static void availabilityTrue() {
        info("31 availability=true: record button may be shown");
    }
}
