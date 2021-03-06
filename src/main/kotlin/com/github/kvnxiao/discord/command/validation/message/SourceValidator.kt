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
import reactor.core.publisher.Mono

/**
 * Checks if the message is non-empty, has an author, and was sent by a non-bot user.
 */
class SourceValidator : MessageValidator {
    override fun validate(value: Message): Mono<Boolean> =
        Mono.just(
            value.content.isNotBlank() &&
                value.author.isPresent &&
                value.author.map { !it.isBot }.orElse(false)
        )
}
