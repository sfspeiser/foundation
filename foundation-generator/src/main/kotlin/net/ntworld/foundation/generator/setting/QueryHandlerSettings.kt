package net.ntworld.foundation.generator.setting

import net.ntworld.foundation.generator.type.ClassInfo
import net.ntworld.foundation.generator.type.HandlerSettings

data class QueryHandlerSettings(
    val query: ClassInfo,
    val version: Int,
    override val bus: String,
    override val handler: ClassInfo,
    override val makeByFactory: Boolean
) : HandlerSettings