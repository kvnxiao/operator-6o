package com.github.kvnxiao.discord.guild.audio.reaction

import com.github.kvnxiao.discord.ReactionUnicode
import com.github.kvnxiao.discord.embeds.addedToQueueDescription
import com.github.kvnxiao.discord.embeds.setAudioEmbedFooter
import com.github.kvnxiao.discord.embeds.setAudioEmbedTitle
import com.github.kvnxiao.discord.guild.audio.GuildAudioState
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

class AudioReactionProcessor(
    private val guildAudioReactionState: GuildAudioReactionState,
    private val guildAudioState: GuildAudioState
) {

    fun processReactionAddEvent(event: ReactionAddEvent): Mono<Void> {
        return Mono.zip(event.message, event.channel, Mono.justOrEmpty(event.guildId))
            .flatMap { (message, channel, guildId) ->
                val audioReactionManager = guildAudioReactionState.getOrCreateForGuild(guildId)
                val audioManager = guildAudioState.getOrCreateForGuild(guildId)
                val index = event.emoji.asUnicodeEmoji().map { ReactionUnicode.getIndexFromDigits(it.raw) }.orElse(-1)
                Mono.justOrEmpty(audioReactionManager.getSelection(event.messageId))
                    .filter { selection -> selection.member.id == event.userId }
                    .filter { selection -> index >= 0 && index < selection.tracks.size }
                    .doOnNext { selection ->
                        audioManager.offer(listOf(selection.tracks[index]), selection.member)
                        audioReactionManager.removeSelection(event.messageId)
                    }
                    .flatMap { selection ->
                        channel.createEmbed { spec ->
                            spec.setAudioEmbedTitle()
                                .addedToQueueDescription(selection.tracks[index])
                                .setAudioEmbedFooter(audioManager.remainingTracks, selection.member)
                        }
                    }
                    .flatMap {
                        message.removeAllReactions()
                            .then(message.addReaction(ReactionEmoji.unicode(ReactionUnicode.FIRST_8_DIGITS[index - 1])))
                    }
            }
            .then()
    }
}
