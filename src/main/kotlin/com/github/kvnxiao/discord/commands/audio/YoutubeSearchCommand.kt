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
import com.github.kvnxiao.discord.embeds.formatIndexed
import com.github.kvnxiao.discord.embeds.setAudioEmbedFooter
import com.github.kvnxiao.discord.embeds.setAudioEmbedTitle
import com.github.kvnxiao.discord.guild.audio.AudioManager
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import com.github.kvnxiao.discord.guild.audio.SourceType
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import java.time.Duration
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
@Id("youtube_search")
@Alias(["yts"])
@Descriptor(
    description = "Searches on YouTube and plays the first search result, or plays a specified youtube link.",
    usage = "%A <query> | %A <youtube URL>"
)
@Permissions(allowDirectMessaging = false)
class YoutubeSearchCommand(
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
                    audioManager.query(query, SourceType.YOUTUBE, true)
                        .collectList()
                        .filter { it.isNotEmpty() }
                        .map { tracks -> tracks.take(8) }
                        .flatMap { tracks ->
                            ctx.channel.createEmbed { spec ->
                                spec.setTitle("Audio Player - Youtube Search")
                                    .setDescription(tracks.formatIndexed())
                                    .setAudioEmbedFooter(audioManager.remainingTracks, member)
                            }.flatMap { message ->
                                Flux.fromIterable(ReactionUnicode.FIRST_8_DIGITS.take(tracks.size))
                                    .flatMap { unicode -> message.addReaction(ReactionEmoji.unicode(unicode)) }
                                    .then(Mono.just(message))
                            }.flatMap { message ->
                                handleReactions(ctx, message, member, tracks, audioManager)
                            }
                        }.onErrorResume {
                            ctx.channel.createMessage("An error occurred with querying for **$query**: ${it.message}")
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
                    .map { ReactionUnicode.getIndexFromDigits(it.raw) > 0 }.orElse(false))

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
            .then(message.addReaction(ReactionEmoji.unicode(ReactionUnicode.FIRST_8_DIGITS[index])))

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
        if (index < 0 || index >= tracks.size) return Mono.empty()
        val track = tracks[index]
        audioManager.offer(listOf(track), member)
        return channel.createEmbed { spec ->
            spec.setAudioEmbedTitle()
                .addedToQueueDescription(track)
                .setAudioEmbedFooter(audioManager.remainingTracks, member)
        }.map { index }
    }
}
