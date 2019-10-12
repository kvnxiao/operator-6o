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

import com.github.kvnxiao.discord.ReactionUnicode
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.github.kvnxiao.discord.guild.audio.SourceType
import com.github.kvnxiao.discord.guild.audio.reaction.AudioSearchSelection
import com.github.kvnxiao.discord.guild.audio.reaction.GuildAudioReactionState
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Id("youtube_search")
@Alias(["yts"])
@Descriptor(
    description = "Searches on YouTube and plays the first search result, or plays a specified youtube link.",
    usage = "%A <query> | %A <youtube URL>"
)
@Permissions(allowDirectMessaging = false)
class YoutubeSearchCommand(
    private val guildAudioState: GuildAudioState,
    private val guildAudioReactionState: GuildAudioReactionState
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null || ctx.args.arguments == null) Mono.empty()
        else {
            val query: String = ctx.args.arguments
            val audioManager = guildAudioState.getOrCreateForGuild(ctx.guild.id)
            ctx.event.message.authorAsMember
                .filter { audioManager.voiceConnectionManager.isVoiceConnected() }
                .flatMap { member ->
                    audioManager.query(query, SourceType.YOUTUBE, true)
                        .collectList()
                        .filter { it.isNotEmpty() }
                        .map { tracks -> tracks.take(8) }
                        .flatMap { tracks ->
                            ctx.channel.createEmbed { spec ->
                                spec.setTitle("Audio Player - Youtube Search")
                                    .setDescription(tracks.format())
                            }.flatMap { message ->
                                Flux.fromIterable(ReactionUnicode.FIRST_8_DIGITS.take(tracks.size))
                                    .flatMap { unicode -> message.addReaction(ReactionEmoji.unicode(unicode)) }
                                    .then(Mono.just(message))
                            }.doOnNext { message ->
                                guildAudioReactionState.getOrCreateForGuild(ctx.guild.id)
                                    .addSelection(message.id, AudioSearchSelection(member, tracks))
                            }
                        }
                        .onErrorResume { ctx.channel.createMessage("An error occurred with querying for: $query") }
                }
                .then()
        }

    private fun List<AudioTrack>.format(): String =
        this.mapIndexed { index, audioTrack ->
            "**${index + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})**"
        }.joinToString(separator = "\n")
}
