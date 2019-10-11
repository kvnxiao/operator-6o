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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

/**
 * Adapter for different event handlers as method overrides
 */
interface AudioEventAdapter : AudioEventListener {
    /**
     * @param player Audio player
     */
    fun onPlayerPause(player: AudioPlayer) {
    }

    /**
     * @param player Audio player
     */
    fun onPlayerResume(player: AudioPlayer) {
    }

    /**
     * @param player Audio player
     * @param track Audio track that started
     */
    fun onTrackStart(
        player: AudioPlayer,
        track: AudioTrack
    ) {
    }

    /**
     * @param player Audio player
     * @param track Audio track that ended
     * @param endReason The reason why the track stopped playing
     */
    fun onTrackEnd(
        player: AudioPlayer,
        track: AudioTrack,
        endReason: AudioTrackEndReason
    ) {
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param exception The exception that occurred
     */
    fun onTrackException(
        player: AudioPlayer,
        track: AudioTrack,
        exception: FriendlyException
    ) {
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param thresholdMs The wait threshold that was exceeded for this event to trigger
     */
    fun onTrackStuck(
        player: AudioPlayer,
        track: AudioTrack,
        thresholdMs: Long
    ) {
    }

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is PlayerPauseEvent -> onPlayerPause(event.player)
            is PlayerResumeEvent -> onPlayerResume(event.player)
            is TrackStartEvent -> onTrackStart(event.player, event.track)
            is TrackEndEvent -> onTrackEnd(
                event.player,
                event.track,
                event.endReason
            )
            is TrackExceptionEvent -> onTrackException(
                event.player,
                event.track,
                event.exception
            )
            is TrackStuckEvent -> onTrackStuck(
                event.player,
                event.track,
                event.thresholdMs
            )
        }
    }
}
