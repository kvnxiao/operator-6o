package com.github.kvnxiao.discord.guild.audio.reaction

import com.github.kvnxiao.discord.guild.GuildState
import discord4j.core.`object`.util.Snowflake

class GuildAudioReactionState : GuildState<AudioReactionManager> {

    private val guildReactionManager: MutableMap<Snowflake, AudioReactionManager> = mutableMapOf()

    override fun getState(guildId: Snowflake): AudioReactionManager? = guildReactionManager[guildId]

    override fun createForGuild(guildId: Snowflake): AudioReactionManager =
        AudioReactionManager().apply { guildReactionManager[guildId] = this }
}
