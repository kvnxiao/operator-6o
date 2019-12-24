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
package com.github.kvnxiao.discord.embeds

import com.github.kvnxiao.discord.reaction.ReactionUnicode
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import java.util.concurrent.TimeUnit

private const val TITLE = "Audio Player"

fun EmbedCreateSpec.initAudioEmbed(queueSize: Int, user: User): EmbedCreateSpec =
    this.setTitle(TITLE)
        .setFooter("$queueSize tracks left in the queue", user.avatarUrl)

fun EmbedCreateSpec.addedToQueue(track: AudioTrack): EmbedCreateSpec =
    this.setDescription("Added **[${track.info.title}](${track.info.uri})** to the queue.")

fun EmbedCreateSpec.addedToQueue(tracks: List<AudioTrack>): EmbedCreateSpec =
    if (tracks.size > 1) {
        this.setDescription("Added ${tracks.size} tracks to the queue.")
    } else {
        this.addedToQueue(tracks[0])
    }

fun EmbedCreateSpec.searchResultIndexed(tracks: List<AudioTrack>): EmbedCreateSpec =
    this.setDescription(tracks.mapIndexed { index, audioTrack ->
        "**${index + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})**"
    }.joinToString(separator = "\n"))

fun EmbedCreateSpec.nowPlaying(track: AudioTrack?, queueList: List<AudioTrack>): EmbedCreateSpec =
    if (track == null) this.setDescription("No tracks are currently playing.")
    else this.setDescription(
        ReactionUnicode.ARROW_FORWARD +
                " ${track.markdown()}\n${track.position.formatTime()}/${track.duration.formatTime()}"
    )
        .addField("Up Next", queueList.markdown(), false)

private fun AudioTrack.markdown(): String =
    "**[${this.info.title}](${this.info.uri}) (${this.duration.formatTime()})**"

private fun List<AudioTrack>.markdown(): String =
    if (this.isEmpty()) "No tracks left."
    else this.joinToString(separator = "\n", transform = AudioTrack::markdown)

private fun Long.formatTime(): String {
    val h = TimeUnit.MILLISECONDS.toHours(this)
    val m = TimeUnit.MILLISECONDS.toMinutes(this)
    val s = TimeUnit.MILLISECONDS.toSeconds(this)
    return if (h > 0) String.format(
        "%02d:%02d:%02d", h, m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
    ) else String.format(
        "%02d:%02d", m % TimeUnit.HOURS.toMinutes(1), s % TimeUnit.MINUTES.toSeconds(1)
    )
}
