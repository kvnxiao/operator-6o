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
package com.github.kvnxiao.discord.command.executable

import com.github.kvnxiao.discord.command.context.Context
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono

/**
 * Abstract command class specifying a command that only works in a guild. Performs a check on the guild in the context
 * and returns an empty Mono if the guild is null.
 */
abstract class GuildCommand : Command {
    final override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null) Mono.empty() else execute(ctx, ctx.guild)

    abstract fun execute(ctx: Context, guild: Guild): Mono<Void>
}
