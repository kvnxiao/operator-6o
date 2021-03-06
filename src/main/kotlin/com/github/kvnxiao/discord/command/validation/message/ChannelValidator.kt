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
package com.github.kvnxiao.discord.command.validation.message

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.PrivateChannel
import discord4j.core.`object`.entity.channel.TextChannel
import reactor.core.publisher.Mono

/**
 * Validates that the command message is either a direct message or a guild text channel message.
 */
class ChannelValidator : MessageValidator {
    override fun validate(value: Message): Mono<Boolean> =
        value.channel.map { channel ->
            channel is TextChannel || channel is PrivateChannel
        }
}
