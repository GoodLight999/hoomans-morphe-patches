package hooman.morphe.patches.dialer.callrecording

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private val recordingAnnouncementResourceNames = setOf(
    "call_recording_starting_voice",
    "call_recording_ending_voice",
    "call_recording_speaker_starting_voice",
    "call_recording_speaker_ending_voice",
)

@Suppress("unused")
val silenceCallRecordingAnnouncementsPatch = resourcePatch(
    name = "Silence call recording announcements",
    description = "Silences Google Phone's spoken start and stop announcements when using the built-in call recorder.",
) {
    compatibleWith(
        Compatibility(
            name = "Google Phone",
            packageName = "com.google.android.dialer",
            appIconColor = 0x1A73E8,
            targets = listOf(
                AppTarget("161.0.726587057"),
                AppTarget("161.0.726587057-downloadable"),
            ),
        ),
    )

    dependsOn(enableCallRecordingPatch)

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
    }
}
