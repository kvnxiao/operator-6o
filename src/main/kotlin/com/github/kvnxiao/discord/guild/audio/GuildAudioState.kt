/**
 *   Copyright 2019 Ze Hao (Kevin) Xiao
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.kvnxiao.discord.guild.audio

import com.github.kvnxiao.discord.guild.GuildState
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.`object`.util.Snowflake

class GuildAudioState : GuildState<AudioManager> {
    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        this.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
    }

    private val guildAudioManager: MutableMap<Snowflake, AudioManager> = mutableMapOf()

    override fun getState(guildId: Snowflake): AudioManager? = guildAudioManager[guildId]

    override fun createForGuild(guildId: Snowflake): AudioManager =
        AudioManager(playerManager).apply { guildAudioManager[guildId] = this }
}
