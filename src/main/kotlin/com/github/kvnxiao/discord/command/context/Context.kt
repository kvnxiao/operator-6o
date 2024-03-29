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
package com.github.kvnxiao.discord.command.context

import com.github.kvnxiao.discord.command.Alias
import com.github.kvnxiao.discord.command.Id
import com.github.kvnxiao.discord.command.descriptor.Descriptor
import com.github.kvnxiao.discord.command.permission.Permissions
import com.github.kvnxiao.discord.command.ratelimit.RateLimits
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.voice.VoiceConnectionRegistry

/**
 * A context data class that contains all the information a Discord command would need to successfully execute.
 * Created during the message event processing and is passed all the way down to the command executable.
 */
data class Context(
    val event: MessageCreateEvent,
    val channel: MessageChannel,
    val guild: Guild?,
    val user: User,
    val args: Arguments,
    val id: Id,
    val alias: Alias,
    val descriptor: Descriptor,
    val permissions: Permissions,
    val rateLimits: RateLimits,
    val isBotOwner: Boolean,
    val isDirectMessage: Boolean,
    val wasBotMentioned: Boolean
) {
    val voiceConnections: VoiceConnectionRegistry
        get() = event.client.voiceConnectionRegistry
}
