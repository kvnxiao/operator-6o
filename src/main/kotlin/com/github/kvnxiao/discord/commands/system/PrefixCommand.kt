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
package com.github.kvnxiao.discord.commands.system

import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.annotation.SubCommand
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.executable.StubCommand
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("prefix")
@SubCommand([PrefixGetCommand::class, PrefixSetCommand::class])
@Permissions(requireGuildOwner = true, requireBotMention = true)
class PrefixCommand : StubCommand

@Component
@Id("prefix.set")
@Alias(["set"])
@Permissions(requireGuildOwner = true, requireBotMention = true)
class PrefixSetCommand(
    private val prefixSettings: PrefixSettings
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null || ctx.args.arguments == null) Mono.empty()
        else prefixSettings.setPrefix(ctx.guild.id, ctx.args.arguments).then()
}

@Component
@Id("prefix.get")
@Alias(["get"])
@Permissions(requireGuildOwner = true, requireBotMention = true)
class PrefixGetCommand(
    private val prefixSettings: PrefixSettings
) : Command {
    override fun execute(ctx: Context): Mono<Void> =
        if (ctx.guild == null) Mono.empty()
        else prefixSettings.loadPrefix(ctx.guild.id)
            .flatMap { ctx.channel.createMessage(it) }
            .then()
}
