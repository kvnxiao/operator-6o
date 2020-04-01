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
import com.github.kvnxiao.discord.embeds.addedToQueue
import com.github.kvnxiao.discord.embeds.initAudioEmbed
import com.github.kvnxiao.discord.embeds.searchResultIndexed
import com.github.kvnxiao.discord.guild.audio.AudioManager
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.github.kvnxiao.discord.guild.audio.SourceType
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
@Id("youtube_search")
@Alias(["yts"])
@Descriptor(
    description = "Searches on YouTube for a list of audio selections to pick and queue up.",
    usage = "%A <query>"
)
@Permissions(allowDirectMessaging = false)
class YoutubeSearchCommand(
    private val guildAudioState: GuildAudioState
) : GuildCommand() {

    companion object {
        const val SEARCH_SIZE: Int = 8
    }

    override fun execute(ctx: Context, guild: Guild): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .flatMap { query ->
                val audioManager = guildAudioState.getOrCreateForGuild(guild.id)
                ctx.event.message.authorAsMember
                    .filter { audioManager.voiceConnectionManager.isVoiceConnected() }
                    .flatMap { member ->
                        audioManager.query(query, SourceType.YOUTUBE, true)
                            .collectList()
                            .filter { it.isNotEmpty() }
                            .map { tracks -> tracks.take(SEARCH_SIZE) }
                            .flatMap { tracks ->
                                ctx.channel.createEmbed { spec ->
                                    spec.initAudioEmbed(audioManager.remainingTracks, member)
                                        .searchResultIndexed(tracks)
                                }.flatMap { message ->
                                    Flux.fromIterable(ReactionUnicode.DIGITS_FROM_1.take(tracks.size))
                                        .flatMap { unicode -> message.addReaction(ReactionEmoji.unicode(unicode)) }
                                        .then(Mono.just(message))
                                }.flatMap { message ->
                                    handleReactions(ctx, message, member, tracks, audioManager)
                                }
                            }.onErrorResume {
                                ctx.channel.createMessage("An error occurred querying for **$query**: ${it.message}")
                                    .then()
                            }
                            .then()
                    }
            }

    /**
     * Handles the reactions for the search result embed. Takes the first result
     */
    private fun handleReactions(
        ctx: Context,
        message: Message,
        member: Member,
        tracks: List<AudioTrack>,
        audioManager: AudioManager
    ): Mono<Void> =
        ctx.event.client.eventDispatcher.on(ReactionAddEvent::class.java)
            .filter { event -> validateReactionEvent(event, message, member) }
            .take(1).take(Duration.ofSeconds(10))
            .next()
            .flatMap { event -> chooseSelection(event, ctx.channel, tracks, member, audioManager) }
            .flatMap { index -> cleanupReactions(message, index) }
            .switchIfEmpty(timeoutReactions(message))

    /**
     * Filter validation for selecting only valid reaction events where the message id matches that of the original
     * search result embed, the issuing user is the same, and the reaction is a valid digit selection within the index
     * bounds.
     */
    private fun validateReactionEvent(
        event: ReactionAddEvent,
        message: Message,
        member: Member
    ): Boolean =
        event.messageId == message.id &&
                event.userId == member.id &&
                (event.emoji.asUnicodeEmoji()
                    .map { ReactionUnicode.getIndexFromDigits(it.raw) >= 0 }.orElse(false))

    /**
     * Cleans up all reactions from the search result embed, and then adds a :timer: emoji to signal that the search
     * result embed has timed out from listening for the original user who issued the command.
     */
    private fun timeoutReactions(message: Message): Mono<Void> =
        message.removeAllReactions().then(message.addReaction(ReactionEmoji.unicode(ReactionUnicode.TIMER)))

    /**
     * Cleans up all reactions from the search result embed, and then adds the corresponding number digit emoji to
     * signal the selected track to offer to the audio player.
     */
    private fun cleanupReactions(message: Message, index: Int): Mono<Void> =
        message.removeAllReactions()
            .then(message.addReaction(ReactionEmoji.unicode(ReactionUnicode.DIGITS_FROM_1[index])))

    /**
     * Converts the selected reaction emoji into a valid index and offers the corresponding track to the audio player,
     * then sends an embed to signal that the selected track has been added to the queue.
     */
    private fun chooseSelection(
        event: ReactionAddEvent,
        channel: MessageChannel,
        tracks: List<AudioTrack>,
        member: Member,
        audioManager: AudioManager
    ): Mono<Int> {
        val index = event.emoji.asUnicodeEmoji().map { ReactionUnicode.getIndexFromDigits(it.raw) }.orElse(-1)
        if (index !in tracks.indices) return Mono.empty()
        val track = tracks[index]
        audioManager.offer(listOf(track), member)
        return channel.createEmbed { spec ->
            spec.initAudioEmbed(audioManager.remainingTracks, member)
                .addedToQueue(track)
        }.map { index }
    }
}
