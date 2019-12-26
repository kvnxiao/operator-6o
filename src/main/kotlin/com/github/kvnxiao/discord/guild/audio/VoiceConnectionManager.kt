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

import discord4j.voice.VoiceConnection
import reactor.core.publisher.Mono

/**
 * Class that keeps track of the current voice connection for a specific guild
 */
class VoiceConnectionManager {
    private var voiceConnection: VoiceConnection? = null

    /**
     * Keeps track of a provided voice connection and sets it as the current voice connection for this guild.
     */
    fun saveVoiceConnection(vc: VoiceConnection) {
        voiceConnection = vc
    }

    /**
     * Disconnects from the current voice connection for this guild, if the connection is active.
     */
    fun disconnectVoiceConnection(): Mono<Void> =
        Mono.justOrEmpty(voiceConnection)
            .flatMap { it.disconnect() }
            .thenEmpty { voiceConnection = null }

    /**
     * Checks whether a current voice connection exists or not.
     */
    fun isVoiceConnected(): Boolean = voiceConnection != null
}
