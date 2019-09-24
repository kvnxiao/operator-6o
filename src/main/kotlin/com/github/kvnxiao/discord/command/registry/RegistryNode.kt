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

interface RegistryNode {
    /**
     * Registers a command and returns the next registry node associated with the new command (to easily register sub-
     * commands).
     */
    fun register(command: DiscordCommand): RegistryNode
    fun subNodeFromAlias(alias: Alias): RegistryNode?
    fun subNodeFromId(id: Id): RegistryNode?

    fun commandFromAlias(alias: Alias): DiscordCommand?
    fun commandFromId(id: Id): DiscordCommand?

    val aliasEntries: Set<Pair<Alias, Id>>
    val idEntries: List<Id>
}
