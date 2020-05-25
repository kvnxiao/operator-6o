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
package com.github.kvnxiao.discord.commands.help

import com.github.kvnxiao.discord.command.CommandProperties
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import com.github.kvnxiao.discord.d4j.botMention
import com.github.kvnxiao.discord.d4j.embed
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Id("all")
@Alias(["all", "commands"])
@Descriptor(
    description = "Displays all available top-level commands.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = true)
class AllCommand(
    private val prefixSettings: PrefixSettings,
    private val propertiesRegistry: PropertiesRegistry
) : Command {
    override fun execute(ctx: Context): Mono<Void> {
        val prefix = prefixSettings.getPrefixOrDefault(ctx.guild?.id)
        val validProperties = propertiesRegistry.topLevelProperties
            .filter { props ->
                validDirectMessage(ctx, props) &&
                        validOwner(ctx, props) &&
                        validGuildOwner(ctx, props)
            }.partition { props -> !props.permissions.requireBotMention }

        val prefixedAliases = validProperties.first
            .flatMap { it.aliases }
            .sorted()
        val mentionAliases = validProperties.second
            .flatMap { it.aliases }
            .sorted()
        val botMention = ctx.event.client.botMention()

        return ctx.channel.createEmbed(
            embed {
                setTitle("Command Manual")
                addField(
                    "List of all top-level commands",
                    prefixedAliases.joinToString(separator = " ") { "`$prefix$it`" },
                    false
                )
                addField(
                    "Commands that require an `@` mention to the bot",
                    mentionAliases.joinToString { "$botMention `$it`" },
                    false
                )
                setFooter(
                    "Displaying valid commands for ${ctx.user.username}#${ctx.user.discriminator}",
                    ctx.user.avatarUrl
                )
                propertiesRegistry.getTopLevelPropertyById("help")?.let { props ->
                    val firstAlias = props.aliases.first()
                    val aliases = props.aliases.joinToString(separator = " or ") { "`$prefix$it`" }
                    addField(
                        "More information about a command",
                        "$aliases <command alias without prefix>\ne.g. `$prefix$firstAlias $firstAlias`",
                        false
                    )
                }
            }
        ).then()
    }

    private fun validDirectMessage(ctx: Context, props: CommandProperties): Boolean =
        !ctx.isDirectMessage || props.permissions.allowDirectMessaging

    private fun validOwner(ctx: Context, props: CommandProperties): Boolean =
        !props.permissions.requireBotOwner || ctx.isBotOwner

    private fun validGuildOwner(ctx: Context, props: CommandProperties): Boolean =
        if (ctx.guild != null) {
            !props.permissions.requireGuildOwner || ctx.guild.ownerId == ctx.user.id
        } else true
}
