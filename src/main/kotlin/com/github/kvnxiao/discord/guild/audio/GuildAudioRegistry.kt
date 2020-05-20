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

import com.github.kvnxiao.discord.guild.GuildRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import java.util.concurrent.ConcurrentHashMap
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class GuildAudioRegistry : GuildRegistry<AudioManager>, DisposableBean {
    companion object {
        private val playerManager: AudioPlayerManager =
            DefaultAudioPlayerManager().apply {
                configuration.setFrameBufferFactory { bufferDuration, format, stopping ->
                    NonAllocatingAudioFrameBuffer(
                        bufferDuration,
                        format,
                        stopping
                    )
                }
                AudioSourceManagers.registerRemoteSources(this)
            }
    }

    private val map: MutableMap<Long, AudioManager> = ConcurrentHashMap()

    override fun get(guildId: Long): Mono<AudioManager> =
        Mono.fromCallable { map[guildId] }

    override fun create(guildId: Long): Mono<AudioManager> =
        Mono.fromCallable { map.put(guildId, AudioManager(playerManager)) }

    override fun destroy() {
        map.entries.toFlux()
            .doOnNext { (_, manager) ->
                manager.stop()
                manager.clearQueue()
            }
            .blockLast()
    }
}
