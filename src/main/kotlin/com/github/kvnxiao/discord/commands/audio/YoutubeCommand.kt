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
import com.github.kvnxiao.discord.command.executable.GuildCommand
import com.github.kvnxiao.discord.d4j.embed
import com.github.kvnxiao.discord.guild.audio.AudioManager
import com.github.kvnxiao.discord.guild.audio.GuildAudioRegistry
import com.github.kvnxiao.discord.guild.audio.SourceType
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@Component
@Id("youtube")
@Alias(["yt"])
@Descriptor(
    description = "Searches on YouTube and plays the first search result, or plays a specified youtube link.",
    usage = "%A <query> | %A <youtube URL>"
)
@Permissions(allowDirectMessaging = false)
class YoutubeCommand(
    private val audioRegistry: GuildAudioRegistry
) : GuildCommand() {
    companion object {
        val singleLink: Regex = Regex("^https://(www|m|music)\\.youtube\\.com/watch\\?v=[a-zA-Z0-9_-]{11}$")
        val singleDirectLink: Regex = Regex("^https://youtu\\.be/[a-zA-Z0-9_-]{11}$")
        val playlistLink: Regex =
            Regex("^https://(www|m|music)\\.youtube\\.com/(watch\\?v=[a-zA-Z0-9_-]{11}&list=|playlist\\?list=)(PL|LL|FL|UU)[a-zA-Z0-9_-]+.*$")
    }

    override fun execute(ctx: Context, guild: Guild): Mono<Void> =
        Mono.zip(
            Mono.justOrEmpty(ctx.args.arguments),
            audioRegistry.getOrCreateFirst(guild.id.asLong()),
            ctx.voiceConnections.getVoiceConnection(guild.id)
        )
            .filterWhen { (_, _, voiceConnection) -> voiceConnection.isConnected }
            .flatMap { (query, audioManager) ->
                ctx.event.message.authorAsMember
                    .flatMap { member ->
                        youtube(ctx, member, query, audioManager, sourceType(query))
                    }
            }.then()

    private fun sourceType(query: String): SourceType {
        return if (singleLink.matches(query) || singleDirectLink.matches(query)) {
            SourceType.YOUTUBE_DIRECT
        } else if (playlistLink.matches(query)) {
            SourceType.YOUTUBE_PLAYLIST
        } else {
            SourceType.YOUTUBE
        }
    }

    private fun youtube(
        ctx: Context,
        member: Member,
        query: String,
        audioManager: AudioManager,
        sourceType: SourceType
    ): Mono<Message> =
        audioManager.query(query, sourceType, sourceType == SourceType.YOUTUBE_PLAYLIST)
            .collectList()
            .filter { it.isNotEmpty() }
            .doOnNext { tracks -> audioManager.offer(tracks, member) }
            .flatMap { tracks ->
                ctx.channel.createEmbed(
                    embed {
                        initAudioEmbed(audioManager.remainingTracks, member)
                        addedToQueue(tracks)
                    }
                )
            }
            .onErrorResume {
                ctx.channel.createMessage("An error occurred querying for **$query**: ${it.message}")
            }
}
