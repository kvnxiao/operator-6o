package com.github.kvnxiao.discord.guild.audio.reaction

import discord4j.core.`object`.util.Snowflake

class AudioReactionManager {

    private val messageMap: MutableMap<Snowflake, AudioSearchSelection> = mutableMapOf()

    fun getSelection(messageId: Snowflake): AudioSearchSelection? = messageMap[messageId]

    fun removeSelection(messageId: Snowflake) {
        messageMap.remove(messageId)
    }

    fun addSelection(messageId: Snowflake, selection: AudioSearchSelection) {
        messageMap[messageId] = selection
    }
}
