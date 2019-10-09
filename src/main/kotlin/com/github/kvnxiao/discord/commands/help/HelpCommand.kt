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
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import reactor.core.publisher.Mono

@Id("help")
@Alias(["help", "man"])
class HelpCommand(
    private val propertiesRegistry: PropertiesRegistry
) : CommandExecutable {
    override fun execute(ctx: Context): Mono<Void> {
        val properties: Mono<Pair<CommandProperties, List<String>>> =
            Mono.justOrEmpty(propertiesRegistry.getPropertiesFromAlias(ctx.args.next()))
        return properties.flatMap { (props, subAliases) ->
            ctx.channel.createMessage { spec ->
                spec.setEmbed { embedSpec ->
                    embedSpec.setTitle("Command Manual")
                        .addField("ID", props.id, true)
                        .addField("Aliases", props.aliases.joinToString(), true)
                        .addField("Sub-commands", if (subAliases.isEmpty()) "N/A" else subAliases.joinToString(), false)
                        .addField("Description", props.descriptor.description, false)
                        .addField("Usage", props.descriptor.usage, false)
                }
            }
        }.then()
    }
}
