package net.ntworld.foundation.generator

import net.ntworld.foundation.generator.setting.EventSettings
import net.ntworld.foundation.generator.type.ClassInfo
import java.lang.Math.min

internal object Utility {
    fun findEventConverterTarget(settings: EventSettings): ClassInfo {
        return ClassInfo(
            className = "${settings.event.className}Converter",
            packageName = findTargetNamespace(settings.event.packageName)
        )
    }

    fun findEventDataMessageConverterTarget(settings: EventSettings): ClassInfo {
        return ClassInfo(
            className = "${settings.event.className}DataMessageConverter",
            packageName = findTargetNamespace(settings.event.packageName)
        )
    }

    fun findEventDataTarget(settings: EventSettings): ClassInfo {
        return ClassInfo(
            className = "${settings.event.className}Data",
            packageName = findTargetNamespace(settings.event.packageName)
        )
    }

    fun buildGeneratedFile(target: ClassInfo, content: String): GeneratedFile {
        val directory = target.packageName.replace(".", "/")
        val fileName = target.className + ".kt"
        return GeneratedFile(
            directory = "/$directory",
            fileName = fileName,
            path = "/$directory/$fileName",
            content = content
        )
    }

    fun findInfrastructureProviderTarget(settings: GeneratorSettings): ClassInfo {
        var packageName = ""
        settings.events.forEach {
            packageName = this.guessPackageName(packageName, it.event.packageName)
        }
        return ClassInfo(
            packageName = packageName,
            className = "AutoGeneratedInfrastructureProvider"
        )
    }

    internal fun guessPackageName(current: String, given: String): String {
        when {
            current.isEmpty() -> return given
            given.isEmpty() -> return current
            given.indexOf(current) == 0 -> return current
            current.indexOf(given) == 0 -> return given
        }

        val currentParts = current.split(".")
        val givenParts = given.split(".")
        val parts = mutableListOf<String>()
        val lastIndex = min(currentParts.lastIndex, givenParts.lastIndex)
        for (i in 0..lastIndex) {
            if (currentParts[i] == givenParts[i]) {
                parts.add(currentParts[i])
            }
        }
        return if (parts.isEmpty()) current else parts.joinToString(".")
    }

    private fun findTargetNamespace(input: String): String {
        return "$input.generated"
    }
}