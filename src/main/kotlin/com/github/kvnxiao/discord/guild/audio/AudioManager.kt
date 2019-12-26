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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import discord4j.core.`object`.entity.Member
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import reactor.core.publisher.Flux

class AudioManager(
    private val playerManager: AudioPlayerManager,
    val voiceConnectionManager: VoiceConnectionManager = VoiceConnectionManager()
) : AudioEventAdapter {
    // Create an audio player for this guild
    private val player: AudioPlayer = playerManager.createPlayer().apply {
        addListener(this@AudioManager)
    }

    private val queue: BlockingDeque<AudioTrack> = LinkedBlockingDeque()

    val provider = LavaPlayerAudioProvider(player)

    fun query(
        query: String,
        sourceType: SourceType = SourceType.UNKNOWN,
        isPlaylist: Boolean = false
    ): Flux<AudioTrack> {
        val lavaplayerQuery = when (sourceType) {
            SourceType.UNKNOWN -> query
            SourceType.YOUTUBE_DIRECT,
            SourceType.YOUTUBE_PLAYLIST -> query
            SourceType.YOUTUBE -> "ytsearch:$query"
            SourceType.SOUNDCLOUD -> "scsearch:$query"
        }

        return Flux.create<AudioTrack> { emitter ->
            playerManager.loadItem(lavaplayerQuery, object : AudioLoadResultHandler {
                override fun loadFailed(exception: FriendlyException) {
                    emitter.error(exception)
                }

                override fun trackLoaded(track: AudioTrack) {
                    emitter.next(track)
                    emitter.complete()
                }

                override fun noMatches() {
                    emitter.complete()
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if (isPlaylist) {
                        playlist.tracks.forEach { track ->
                            emitter.next(track)
                        }
                    } else {
                        emitter.next(playlist.tracks[0])
                    }
                    emitter.complete()
                }
            })
        }
    }

    fun offer(tracks: List<AudioTrack>, requestedBy: Member) {
        tracks.forEach {
            it.userData = requestedBy
            queue.offer(it)
        }
        if (player.playingTrack == null) {
            next(true)
        }
    }

    fun next(noInterrupt: Boolean = false) {
        player.startTrack(queue.poll(), noInterrupt)
    }

    fun shuffle() {
        if (remainingTracks > 0) {
            val shuffledTemp = queue.shuffled()
            queue.clear()
            queue.addAll(shuffledTemp)
        }
    }

    fun stop() {
        if (player.playingTrack != null) player.stopTrack()
    }

    fun getCurrentTrack(): AudioTrack? = player.playingTrack

    val queueList: List<AudioTrack>
        get() = queue.toList()

    val remainingTracks: Int
        get() = queueList.size

    fun clearQueue() = queue.clear()

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            next()
        }
    }
}
