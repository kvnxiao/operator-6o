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
import com.github.kvnxiao.discord.embeds.addedToQueueDescription
import com.github.kvnxiao.discord.embeds.setAudioEmbedFooter
import com.github.kvnxiao.discord.embeds.setAudioEmbedTitle
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.github.kvnxiao.discord.guild.audio.SourceType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("youtube")
@Alias(["yt"])
@Descriptor(
    description = "Searches on YouTube and plays the first search result, or plays a specified youtube link.",
    usage = "%A <query> | %A <youtube URL>"
)
@Permissions(allowDirectMessaging = false)
class YoutubeCommand(
    private val guildAudioState: GuildAudioState
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null || ctx.args.arguments == null) Mono.empty()
        else {
            val query: String = ctx.args.arguments
            val audioManager = guildAudioState.getOrCreateForGuild(ctx.guild.id)
            ctx.event.message.authorAsMember
                .filter { audioManager.voiceConnectionManager.isVoiceConnected() }
                .flatMap { member ->
                    audioManager.query(query, SourceType.YOUTUBE)
                        .collectList()
                        .filter { it.isNotEmpty() }
                        .doOnNext { tracks -> audioManager.offer(tracks, member) }
                        .flatMap { tracks ->
                            ctx.channel.createEmbed { spec ->
                                spec.setAudioEmbedTitle()
                                    .addedToQueueDescription(tracks)
                                    .setAudioEmbedFooter(audioManager.remainingTracks, member)
                            }
                        }
                        .onErrorResume {
                            ctx.channel.createMessage("An error occurred with querying for **$query**: ${it.message}")
                        }
                }
                .then()
        }
}
