group = "io.github.GoodLight999"

patches {
    about {
        name = "Google Phone Morphe Patches"
        description = "Morphe patches for Google Phone call recording."
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

    publish { dependsOn("generatePatchesList") }
}
