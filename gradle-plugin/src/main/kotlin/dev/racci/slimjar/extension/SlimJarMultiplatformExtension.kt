package dev.racci.slimjar.extension

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

public abstract class SlimJarMultiplatformExtension @Inject constructor(
    target: KotlinTarget
) : SlimJarExtension(target.project)