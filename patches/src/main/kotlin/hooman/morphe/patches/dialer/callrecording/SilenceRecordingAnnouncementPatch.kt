package hooman.morphe.patches.dialer.callrecording

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_HOOK =
    "Lapp/morphe/extension/dialer/callrecording/SilentDisclosure;->silence(Ljava/lang/String;)Ljava/lang/String;"

// Log tag kept by Google's logging framework. R8 renames the class, not this string.
private const val AUDIO_INJECTOR_TAG = "com/android/dialer/audio/audioinjector/AudioInjector"

// Kotlin null-check message emitted right after the resolver turns the clip File into a path.
private const val ABSOLUTE_PATH_CHECK = "getAbsolutePath(...)"

private fun Instruction.isFileGetAbsolutePath(): Boolean {
    if (opcode != Opcode.INVOKE_VIRTUAL) return false
    val reference = (this as ReferenceInstruction).reference as? MethodReference ?: return false
    return reference.definingClass == "Ljava/io/File;" &&
        reference.name == "getAbsolutePath" &&
        reference.parameterTypes.isEmpty() &&
        reference.returnType == "Ljava/lang/String;"
}

private fun Method.isAudioPathResolver(): Boolean {
    val instructions = implementation?.instructions?.toList() ?: return false
    return returnType == "Ljava/lang/Object;" &&
        instructions.any { it.isFileGetAbsolutePath() } &&
        instructions.any {
            ((it as? ReferenceInstruction)?.reference as? StringReference)?.string == ABSOLUTE_PATH_CHECK
        }
}

@Suppress("unused")
val silenceRecordingAnnouncementPatch = bytecodePatch(
    name = "Silence call recording announcement",
    description = "Replaces the call recording announcement (\"this call is being recorded\" at start " +
        "and end, including the beep variant) with silence, so neither side hears it. Recording " +
        "itself works as before. Use together with \"Enable call recording\" where the recorder is hidden.",
) {
    compatibleWith(
        Compatibility(
            name = "Google Phone",
            packageName = "com.google.android.dialer",
            appIconColor = 0x1A73E8,
            targets = listOf(
                AppTarget("161.0.726587057"),
                AppTarget("161.0.726587057-downloadable"),
                AppTarget("247.0.1016877637"),
                AppTarget("247.0.1016877637-pixel"),
            ),
        ),
    )

    extendWith("extensions/dialer.mpe")

    execute {
        // Every disclosure (TTS, built-in clip, beep; start and end) is an "injectable audio" that
        // AudioInjector resolves to a file path in one method: `file.getAbsolutePath()` followed by
        // the Kotlin "getAbsolutePath(...)" null check, then returned. That single path feeds both
        // the local player and the injection into the call. Rewrite it right after the
        // move-result: SilentDisclosure returns a silent WAV for disclosure clips and leaves any
        // other clip (Call Screen, voice assist, ...) untouched. Playback is kept because the
        // recorder only starts once the disclosure playback completes.
        val resolvers = classDefByStrings(AUDIO_INJECTOR_TAG)
            .flatMap { classDef -> classDef.methods.filter { it.isAudioPathResolver() }.map { classDef to it } }
        if (resolvers.size != 1) {
            throw PatchException(
                "Google Phone: expected one AudioInjector path resolver, found ${resolvers.size}. Re-derive.",
            )
        }
        val (resolverClass, resolverMethod) = resolvers.single()
        val method = mutableClassDefBy(resolverClass).methods.first {
            it.name == resolverMethod.name &&
                it.returnType == resolverMethod.returnType &&
                it.parameterTypes.map(CharSequence::toString) ==
                resolverMethod.parameterTypes.map(CharSequence::toString)
        }

        val sites = method.instructions.withIndex()
            .filter { (_, instruction) -> instruction.isFileGetAbsolutePath() }
            .map { (index, _) -> index }
        if (sites.isEmpty()) {
            throw PatchException("Google Phone: AudioInjector resolver has no getAbsolutePath call. Re-derive.")
        }
        // Back to front so earlier indices stay valid.
        sites.sortedDescending().forEach { index ->
            val moveResult = method.instructions[index + 1]
            if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) {
                throw PatchException("Google Phone: getAbsolutePath result is not moved. Re-derive.")
            }
            val pathRegister = (moveResult as OneRegisterInstruction).registerA
            method.addInstructions(
                index + 2,
                """
                    invoke-static/range { v$pathRegister .. v$pathRegister }, $EXTENSION_HOOK
                    move-result-object v$pathRegister
                """,
            )
        }
    }
}
