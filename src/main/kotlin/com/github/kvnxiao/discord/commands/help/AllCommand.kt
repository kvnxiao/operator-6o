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

import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import reactor.core.publisher.Mono

@Id("all")
@Alias(["all", "commands"])
@Descriptor(
    description = "Displays all available top-level commands.",
    usage = "%A"
)
class AllCommand(
    private val prefixSettings: PrefixSettings,
    private val propertiesRegistry: PropertiesRegistry
) : Command {
    override fun execute(ctx: Context): Mono<Void> {
        val prefix = prefixSettings.getPrefixOrDefault(ctx.guild)
        val prefixedAliases = propertiesRegistry.topLevelAliasEntries
            .map { prefix + it.first }
            .sorted()
        return ctx.channel.createMessage { spec ->
            spec.setEmbed { embedSpec ->
                embedSpec.setTitle("Command Manual")
                    .addField(
                        "List of all top-level commands",
                        prefixedAliases.joinToString(
                            separator = "`, `",
                            prefix = "`",
                            postfix = "`"
                        ),
                        false
                    )
            }
        }.then()
    }
}
