package dev.racci.slimjar.extension

import io.github.slimjar.relocation.RelocationRule
import io.github.slimjar.resolver.data.Mirror
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

public abstract class SlimJarMultiplatformExtension @Inject constructor(
    target: KotlinTarget
) : SlimJarExtension() {
    final override val relocations: SetProperty<RelocationRule> = target.project.objects.setProperty()

    final override val mirrors: SetProperty<Mirror> = target.project.objects.setProperty()

    final override val compileTimeResolution: Property<Boolean> = target.project.objects.property<Boolean>().convention(true)
}
