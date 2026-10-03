package hooman.morphe.patches.dialer.callrecording

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private val recordingAnnouncementResourceNames = setOf(
    "call_recording_starting_voice",
    "call_recording_ending_voice",
    "call_recording_speaker_starting_voice",
    "call_recording_speaker_ending_voice",
)

private val recordingAnnouncementRawResources = mapOf(
    "starting_voice_beep_sound.ogg" to silentOggBytes,
    "ending_voice_beep_sound.ogg" to silentOggBytes,
    "starting_voice_ar_XA.m4a" to silentM4aBytes,
    "ending_voice_ar_XA.m4a" to silentM4aBytes,
    "starting_voice_my_MM.m4a" to silentM4aBytes,
    "ending_voice_my_MM.m4a" to silentM4aBytes,
)

private val googlePhone161Compatibility = Compatibility(
    name = "Google Phone",
    packageName = "com.google.android.dialer",
    appIconColor = 0x1A73E8,
    targets = listOf(
        AppTarget("161.0.726587057"),
        AppTarget("161.0.726587057-downloadable"),
    ),
)

private val silenceCallRecordingAnnouncementsResourcePatch = resourcePatch(
    description = "Silences TTS, built-in voice, and beep call-recording start and stop announcements.",
) {
    compatibleWith(googlePhone161Compatibility)

    execute {
        val foundResourceNames = mutableSetOf<String>()
        val valuesDirectories = get("res")
            .listFiles()
            ?.filter { file -> file.isDirectory && file.name.startsWith("values") }
            .orEmpty()

        valuesDirectories.forEach { directory ->
            directory.listFiles()
                ?.filter { file -> file.isFile && file.extension == "xml" }
                ?.forEach { xml ->
                    val relativePath = "res/${directory.name}/${xml.name}"

                    document(relativePath).use { document ->
                        val stringNodes = document.getElementsByTagName("string")
                        for (index in 0 until stringNodes.length) {
                            val element = stringNodes.item(index) as? Element ?: continue
                            val name = element.getAttribute("name")
                            if (name in recordingAnnouncementResourceNames) {
                                element.textContent = ""
                                foundResourceNames += name
                            }
                        }

                        val itemNodes = document.getElementsByTagName("item")
                        for (index in 0 until itemNodes.length) {
                            val element = itemNodes.item(index) as? Element ?: continue
                            if (element.getAttribute("type") != "string") continue

                            val name = element.getAttribute("name")
                            if (name in recordingAnnouncementResourceNames) {
                                element.textContent = ""
                                foundResourceNames += name
                            }
                        }
                    }
                }
        }

        val required = setOf(
            "call_recording_starting_voice",
            "call_recording_ending_voice",
        )
        val missing = required - foundResourceNames
        if (missing.isNotEmpty()) {
            throw PatchException(
                "Google Phone: recording announcement resources were not found: " +
                    missing.joinToString() +
                    ". The call-recording prompt implementation changed.",
            )
        }

        val rawDirectory = get("res/raw")
        val missingRawResources = recordingAnnouncementRawResources.keys.filter { name ->
            !rawDirectory.resolve(name).isFile
        }
        if (missingRawResources.isNotEmpty()) {
            throw PatchException(
                "Google Phone: recording announcement raw resources were not found: " +
                    missingRawResources.joinToString() +
                    ". The call-recording disclosure implementation changed.",
            )
        }

        recordingAnnouncementRawResources.forEach { (name, bytes) ->
            rawDirectory.resolve(name).writeBytes(bytes)
        }
    }
}

@Suppress("unused")
val silentCallRecordingPatch = bytecodePatch(
    name = "Silent call recording",
    description = "Enables Google Phone's built-in call recorder and silences TTS, built-in voice, and beep start/stop announcements.",
) {
    compatibleWith(googlePhone161Compatibility)
    dependsOn(silenceCallRecordingAnnouncementsResourcePatch)

    execute {
        val canRecord = classDefByStrings(
            "Call recording is disabled in the current country",
        ).singleOrNull()
            ?: throw PatchException(
                "Google Phone: CanRecord class not found or ambiguous. The call-recording gate changed.",
            )
        val mutableCanRecord = mutableClassDefBy(canRecord)

        val availability = mutableCanRecord.methods.filter { method ->
            method.returnType == "Z" && method.parameterTypes.isEmpty()
        }
        if (availability.size != 1) {
            throw PatchException(
                "Google Phone: expected one no-arg boolean availability method on CanRecord, found " +
                    "${availability.size}. Re-derive.",
            )
        }
        availability.single().addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
