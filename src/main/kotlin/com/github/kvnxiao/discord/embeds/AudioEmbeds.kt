package com.github.kvnxiao.discord.embeds

import com.github.kvnxiao.discord.ReactionUnicode
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import java.util.concurrent.TimeUnit

// Embed create spec setters

fun EmbedCreateSpec.setAudioEmbedTitle(): EmbedCreateSpec =
    this.setTitle("Audio Player")

fun EmbedCreateSpec.setAudioEmbedFooter(queueSize: Int, user: User): EmbedCreateSpec =
    this.setFooter("$queueSize tracks left in the queue", user.avatarUrl)

fun EmbedCreateSpec.addedToQueueDescription(track: AudioTrack): EmbedCreateSpec =
    this.setDescription(
        "Added **[${track.info.title}](${track.info.uri})** to the queue."
    )

fun EmbedCreateSpec.addedToQueueDescription(tracks: List<AudioTrack>): EmbedCreateSpec =
    if (tracks.size > 1) {
        this.setDescription("Added ${tracks.size} tracks to the queue.")
    } else {
        this.addedToQueueDescription(tracks[0])
    }

fun EmbedCreateSpec.displayTrack(track: AudioTrack?, queueList: List<AudioTrack>): EmbedCreateSpec =
    if (track == null) this.setDescription("No tracks are currently playing.")
    else this.setDescription("${ReactionUnicode.ARROW_FORWARD} ${track.formatAsMarkdown()}\n${track.position.formatTime()}/${track.duration.formatTime()}")
        .addField("Up Next", queueList.formatAsMarkdown(), false)

// Audio track formatting for embed descriptions

fun AudioTrack.formatAsMarkdown(): String =
    "**[${this.info.title}](${this.info.uri}) (${this.duration.formatTime()})**"

fun List<AudioTrack>.formatAsMarkdown(): String =
    if (this.isEmpty()) "No tracks left."
    else this.joinToString(separator = "\n") { it.formatAsMarkdown() }

fun List<AudioTrack>.formatIndexed(): String =
    this.mapIndexed { index, audioTrack ->
        "**${index + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})**"
    }.joinToString(separator = "\n")

fun Long.formatTime(): String {
    val h = TimeUnit.MILLISECONDS.toHours(this)
    val m = TimeUnit.MILLISECONDS.toMinutes(this)
    val s = TimeUnit.MILLISECONDS.toSeconds(this)
    return if (h > 0) String.format(
        "%02d:%02d:%02d", h, m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
    ) else String.format(
        "%02d:%02d", m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
    )
}
