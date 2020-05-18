package com.github.kvnxiao.discord.d4j

import discord4j.core.spec.EmbedCreateSpec
import java.util.function.Consumer

@DslMarker
annotation class EmbedDSL

@EmbedDSL
fun embed(block: EmbedCreateSpec.() -> Unit): Consumer<in EmbedCreateSpec> =
    Consumer { spec ->
        spec.block()
    }
