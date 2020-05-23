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
package com.github.kvnxiao.discord.command.prefix

import discord4j.common.util.Snowflake
import reactor.core.publisher.Mono

/**
 * Interface with methods defined to load command prefixes for each guild.
 */
interface PrefixSettings {
    companion object {
        const val DEFAULT_PREFIX: String = "!"
    }

    /**
     * Loads the guild specific prefix for a single guild ID snowflake.
     * @return a [Mono] containing the guild prefix loaded for the guild, or an empty [Mono] upon failure to load.
     */
    fun loadPrefixForGuild(guildId: Snowflake): Mono<String>

    /**
     * Sets the guild specific prefix for a single guild ID snowflake.
     * @return a non-empty [Mono] containing a boolean denoting the status of saving the guild prefix.
     */
    fun setPrefixForGuild(guildId: Snowflake, value: String): Mono<Boolean>

    /**
     * Gets the guild specific prefix for a single guild ID snowflake, and defaults to a default value [DEFAULT_PREFIX]
     * if the provided snowflake is null or if the guild does not have a custom prefix set.
     * @return the prefix string for the specified guild if it exists, [DEFAULT_PREFIX] otherwise.
     */
    fun getPrefixOrDefault(guildId: Snowflake?): String
}
