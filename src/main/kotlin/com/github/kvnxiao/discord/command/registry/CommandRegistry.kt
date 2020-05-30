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
package com.github.kvnxiao.discord.command.registry

import com.github.kvnxiao.discord.command.Alias
import com.github.kvnxiao.discord.command.DiscordCommand
import com.github.kvnxiao.discord.command.Id
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger { }

private fun register(
    command: DiscordCommand,
    aliasToIdMap: MutableMap<Alias, Id>,
    idMap: MutableMap<Id, RegistryNode>,
    parentId: Id?
): RegistryNode {
    // Validate that the id is unique
    require(!idMap.containsKey(command.properties.id)) { "The id ${command.properties.id} is already in use!" }

    // Validate that all aliases are unique and have no spaces
    command.properties.aliases.forEach { alias ->
        require(!alias.contains("\\s|\\n".toRegex())) { "The alias $alias contains invalid whitespace characters!" }
        require(!aliasToIdMap.containsKey(alias)) {
            "The alias $alias is already in use (from command id ${command.properties.id})!"
        }
    }

    // Create new command registry node, associate id with the node, and all aliases to the unique id
    val commandNode = MapTreeRegistryNode(command)
    idMap[command.properties.id] = commandNode
    command.properties.aliases.forEach { alias -> aliasToIdMap[alias] = command.properties.id }

    if (parentId == null) {
        logger.info { "Registering command [${command.properties.id}] with aliases ${command.properties.aliases}" }
    } else {
        logger.info { "Registering sub-command [${command.properties.id}] with aliases ${command.properties.aliases} for parent command [$parentId]" }
    }
    return commandNode
}

class MapTreeRegistryRoot : RegistryNode {

    private val aliasToIdMap: MutableMap<Alias, Id> = mutableMapOf()
    private val idMap: MutableMap<Id, RegistryNode> = mutableMapOf()

    override val aliasEntries: Set<Pair<Alias, Id>>
        get() = aliasToIdMap.entries.map { it.toPair() }.toSet()

    override val idEntries: List<Id>
        get() = idMap.keys.toList()

    override fun register(command: DiscordCommand): RegistryNode = register(command, aliasToIdMap, idMap, null)

    override fun subNodeFromAlias(alias: Alias): RegistryNode? =
        idMap[aliasToIdMap[alias]]

    override fun subNodeFromId(id: Id): RegistryNode? = idMap[id]

    override fun commandFromAlias(alias: Alias): DiscordCommand? =
        (idMap[aliasToIdMap[alias]] as MapTreeRegistryNode?)?.command

    override fun commandFromId(id: Id): DiscordCommand? =
        (idMap[id] as MapTreeRegistryNode?)?.command
}

class MapTreeRegistryNode(
    val command: DiscordCommand
) : RegistryNode {

    private val subAliasToIdMap: MutableMap<Alias, Id> = mutableMapOf()
    private val subIdMap: MutableMap<Id, RegistryNode> = mutableMapOf()

    override val aliasEntries: Set<Pair<Alias, Id>>
        get() = subAliasToIdMap.entries.map { it.toPair() }.toSet()

    override val idEntries: List<Id>
        get() = subIdMap.keys.toList()

    override fun register(command: DiscordCommand): RegistryNode =
        register(command, subAliasToIdMap, subIdMap, this.command.properties.id)

    override fun subNodeFromAlias(alias: Alias): RegistryNode? =
        subIdMap[subAliasToIdMap[alias]]

    override fun subNodeFromId(id: Id): RegistryNode? = subIdMap[id]

    override fun commandFromAlias(alias: Alias): DiscordCommand? =
        (subIdMap[subAliasToIdMap[alias]] as MapTreeRegistryNode?)?.command

    override fun commandFromId(id: Id): DiscordCommand? =
        (subIdMap[id] as MapTreeRegistryNode?)?.command
}
