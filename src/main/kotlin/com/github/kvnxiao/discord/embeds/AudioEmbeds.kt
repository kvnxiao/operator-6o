package com.github.kvnxiao.discord.embeds

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec

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
