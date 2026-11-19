package app.morphe.extension.dialer.callrecording;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Swaps Google Phone's call-recording disclosure clips for silence.
 *
 * <p>Every disclosure (TTS "this call is being recorded", the built-in localized clips and the
 * beep variant, both start and end) is resolved by AudioInjector into a file path, and that one
 * path feeds both the local player and the injection into the call. The patch routes that path
 * through {@link #silence(String)}: disclosure clips are replaced by a short silent WAV, every other
 * clip (Call Screen, voice assist, ...) is returned unchanged.</p>
 *
 * <p>The recorder only starts after the disclosure playback completes, so playback is kept and
 * simply made silent.</p>
 */
@SuppressWarnings("unused")
public final class SilentDisclosure {
    // TTS:      call_recording_starting_voice_ja_JP.wav / call_recording_ending_voice_...
    // Built-in: starting_voice_ar_XA, ending_voice_my_MM, starting_voice-ar_XA.m4a, ...
    // Beep:     starting_voice_beep_sound, ending_voice-beep_sound.ogg
    private static final Pattern DISCLOSURE_NAME =
            Pattern.compile("^(call_recording_)?(starting|ending)_voice([_.\\-].*)?$");
    private static final String DISCLOSURE_DIR = "callrecordingprompt";
    private static final String SILENT_NAME = "morphe_silent_disclosure.wav";

    // Mono 16-bit PCM; 16 kHz is divisible by the 50 Hz write rate the voip injectors require.
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int DURATION_MS = 500;

    private static final byte[] SILENT_WAV = buildSilentWav();

    private SilentDisclosure() {
    }

    public static boolean isDisclosure(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && DISCLOSURE_DIR.equals(parent.getName())) {
            return true;
        }
        return DISCLOSURE_NAME.matcher(file.getName()).matches();
    }

    /**
     * @return a silent clip for disclosure paths, otherwise {@code path} unchanged. If the silent
     * clip cannot be prepared, returns a path that does not exist so playback fails instead of
     * announcing the recording.
     */
    public static String silence(String path) {
        if (!isDisclosure(path)) {
            return path;
        }
        File original = new File(path);
        File dir = original.getParentFile();
        try {
            if (dir != null) {
                File silent = new File(dir, SILENT_NAME);
                if (ensureSilentFile(silent)) {
                    return silent.getAbsolutePath();
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return path + ".morphe-unavailable";
    }

    private static boolean ensureSilentFile(File target) throws Exception {
        if (isSilentFile(target)) {
            return true;
        }
        File temp = new File(target.getParentFile(), target.getName() + ".tmp");
        FileOutputStream out = new FileOutputStream(temp);
        try {
            out.write(SILENT_WAV);
            out.getFD().sync();
        } finally {
            out.close();
        }
        if (!temp.renameTo(target)) {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
        return isSilentFile(target);
    }

    private static boolean isSilentFile(File file) {
        if (file.length() != SILENT_WAV.length) {
            return false;
        }
        byte[] current = new byte[SILENT_WAV.length];
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                int offset = 0;
                while (offset < current.length) {
                    int read = in.read(current, offset, current.length - offset);
                    if (read < 0) {
                        return false;
                    }
                    offset += read;
                }
            } finally {
                in.close();
            }
        } catch (Throwable e) {
            return false;
        }
        return Arrays.equals(current, SILENT_WAV);
    }

    private static byte[] buildSilentWav() {
        int blockAlign = CHANNELS * BITS_PER_SAMPLE / 8;
        int byteRate = SAMPLE_RATE * blockAlign;
        int dataSize = byteRate * DURATION_MS / 1000;
        byte[] wav = new byte[44 + dataSize]; // PCM payload stays zero = silence

        int i = 0;
        i = putAscii(wav, i, "RIFF");
        i = putLe32(wav, i, 36 + dataSize);
        i = putAscii(wav, i, "WAVE");
        i = putAscii(wav, i, "fmt ");
        i = putLe32(wav, i, 16);          // fmt chunk size
        i = putLe16(wav, i, 1);           // PCM
        i = putLe16(wav, i, CHANNELS);
        i = putLe32(wav, i, SAMPLE_RATE);
        i = putLe32(wav, i, byteRate);
        i = putLe16(wav, i, blockAlign);
        i = putLe16(wav, i, BITS_PER_SAMPLE);
        i = putAscii(wav, i, "data");
        putLe32(wav, i, dataSize);
        return wav;
    }

    private static int putAscii(byte[] b, int i, String s) {
        for (int k = 0; k < s.length(); k++) {
            b[i++] = (byte) s.charAt(k);
        }
        return i;
    }

    private static int putLe16(byte[] b, int i, int v) {
        b[i++] = (byte) v;
        b[i++] = (byte) (v >>> 8);
        return i;
    }

    private static int putLe32(byte[] b, int i, int v) {
        b[i++] = (byte) v;
        b[i++] = (byte) (v >>> 8);
        b[i++] = (byte) (v >>> 16);
        b[i++] = (byte) (v >>> 24);
        return i;
    }
}
