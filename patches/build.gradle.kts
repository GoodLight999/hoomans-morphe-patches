group = "io.github.arandomhooman"

patches {
    about {
        name = "GoodLight999's Morphe Patches"
        description = "Custom Morphe patches based on Hooman's Morphe Patches."
        source = "https://github.com/GoodLight999/hoomans-morphe-patches"
        author = "GoodLight999"
        contact = "na"
        website = "https://github.com/GoodLight999/hoomans-morphe-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath: Configuration by configurations.creating

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}