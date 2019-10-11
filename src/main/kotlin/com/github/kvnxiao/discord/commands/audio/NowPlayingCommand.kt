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
package com.github.kvnxiao.discord.commands.audio

import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.spec.EmbedCreateSpec
import java.util.concurrent.TimeUnit
import reactor.core.publisher.Mono

@Id("nowplaying")
@Alias(["np", "playing"])
@Descriptor(
    description = "Prints the current audio track being played by the bot.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = false)
class NowPlayingCommand(
    private val guildAudioState: GuildAudioState
) : Command {
    override fun execute(ctx: Context): Mono<Void> {
        return if (ctx.guild == null) Mono.empty()
        else Mono.justOrEmpty(guildAudioState.getState(ctx.guild.id))
            .filter { it.voiceConnectionManager.isVoiceConnected() }
            .flatMap { audioManager ->
                val currentTrack = audioManager.getCurrentTrack()
                val queueList = audioManager.getQueueList()
                ctx.channel.createEmbed { spec ->
                    spec.setTitle("Audio Player - Now Playing")
                        .formatTrack(currentTrack, queueList)
                        .setFooter("${queueList.size} tracks left in queue", ctx.user.avatarUrl)
                }
            }
            .then()
    }

    private fun EmbedCreateSpec.formatTrack(track: AudioTrack?, queueList: List<AudioTrack>): EmbedCreateSpec =
        if (track == null) this.setDescription("No tracks are currently playing.")
        else this.setDescription("\u25B6 ${track.formatAsMarkdown()}\n${track.position.format()}/${track.duration.format()}")
            .addField("Up Next", queueList.formatAsMarkdown(), false)

    private fun AudioTrack.formatAsMarkdown(): String =
        "**[${this.info.title}](${this.info.uri}) (${this.duration.format()})**"

    private fun List<AudioTrack>.formatAsMarkdown(): String =
        if (this.isEmpty()) "No tracks left."
        else this.joinToString(separator = "\n") { it.formatAsMarkdown() }

    private fun Long.format(): String {
        val h = TimeUnit.MILLISECONDS.toHours(this)
        val m = TimeUnit.MILLISECONDS.toMinutes(this)
        val s = TimeUnit.MILLISECONDS.toSeconds(this)
        return if (h > 0) String.format(
            "%02d:%02d:%02d", h, m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
        ) else String.format(
            "%02d:%02d", m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
        )
    }
}
