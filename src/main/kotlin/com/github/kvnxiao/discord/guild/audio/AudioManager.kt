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
package com.github.kvnxiao.discord.guild.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.TextChannel
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class AudioManager(
    private val playerManager: AudioPlayerManager,
    val voiceConnectionManager: VoiceConnectionManager = VoiceConnectionManager()
) : AudioEventAdapter {
    // Create an audio player for this guild
    private val player: AudioPlayer = playerManager.createPlayer()

    private val queue: BlockingDeque<AudioTrack> = LinkedBlockingDeque()

    val provider = LavaPlayerAudioProvider(player)

    fun enqueue(
        query: String,
        user: Member,
        channel: TextChannel,
        sourceType: SourceType = SourceType.UNKNOWN,
        isPlaylist: Boolean = false
    ) {
        val lavaplayerQuery = when (sourceType) {
            SourceType.UNKNOWN -> query
            SourceType.YOUTUBE -> "ytsearch:$query"
            SourceType.SOUNDCLOUD -> "scsearch:$query"
        }

        playerManager.loadItem(lavaplayerQuery, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                channel.createMessage("Failed to load query due to exception: $exception").subscribe()
            }

            override fun trackLoaded(track: AudioTrack) = enqueue(track)

            override fun noMatches() {
                channel.createMessage("No results found for query").subscribe()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) =
                if (isPlaylist) {
                    enqueue(playlist.tracks)
                } else {
                    enqueue(playlist.tracks[0])
                }
        })
    }

    private fun enqueue(track: AudioTrack) {
        if (player.playingTrack == null) {
            player.playTrack(track)
        } else {
            queue.offer(track)
        }
    }

    private fun enqueue(tracks: List<AudioTrack>) {
        tracks.forEach {
            queue.offer(it)
        }
        if (player.playingTrack == null) {
            next(true)
        }
    }

    fun next(noInterrupt: Boolean = false) {
        player.startTrack(queue.poll(), noInterrupt)
    }
}
