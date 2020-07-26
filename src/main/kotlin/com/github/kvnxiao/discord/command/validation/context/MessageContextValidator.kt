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
package com.github.kvnxiao.discord.command.validation.context

import com.github.kvnxiao.discord.command.context.Context
import reactor.core.publisher.Mono

class MessageContextValidator : ContextValidator {
    override fun validate(value: Context): Mono<Boolean> {
        return Mono.just(
            passesHasBotMention(value) &&
                passesRequireDirectMessage(value) &&
                passesAllowDirectMessage(value) &&
                passesIsGuildOwner(value) &&
                passesIsBotOwner(value)
        )
    }

    private fun passesHasBotMention(context: Context): Boolean =
        !(context.permissions.requireBotMention xor context.wasBotMentioned)

    private fun passesRequireDirectMessage(context: Context): Boolean =
        !context.permissions.requireDirectMessaging || context.isDirectMessage

    private fun passesAllowDirectMessage(context: Context): Boolean =
        context.permissions.allowDirectMessaging || !context.isDirectMessage

    private fun passesIsGuildOwner(context: Context): Boolean =
        if (context.guild != null && context.permissions.requireGuildOwner) context.guild.ownerId == context.user.id
        else true

    private fun passesIsBotOwner(context: Context): Boolean =
        if (context.guild != null && context.permissions.requireBotOwner) context.isBotOwner
        else true
}
