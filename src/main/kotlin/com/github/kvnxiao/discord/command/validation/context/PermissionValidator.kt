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
import discord4j.core.`object`.entity.channel.TextChannel
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class PermissionValidator : ContextValidator {
    override fun validate(value: Context): Mono<Boolean> =
        if (value.isDirectMessage) true.toMono()
        else (value.channel as TextChannel)
            .getEffectivePermissions(value.event.member.get().id)
            .map { it.containsAll(value.permissions.permSet) }
}
