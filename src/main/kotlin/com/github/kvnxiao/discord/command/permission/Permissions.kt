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
package com.github.kvnxiao.discord.command.permission

import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

/**
 * A data class containing permission settings associated with a command.
 */
data class Permissions(
    val requireBotOwner: Boolean = REQUIRE_BOT_OWNER,
    val requireGuildOwner: Boolean = REQUIRE_GUILD_OWNER,
    val requireBotMention: Boolean = REQUIRE_BOT_MENTION,
    val allowDirectMessaging: Boolean = ALLOW_DIRECT_MESSAGING,
    val requireDirectMessaging: Boolean = REQUIRE_DIRECT_MESSAGING,
    val removeInvocationMessage: Boolean = REMOVE_INVOCATION_MESSAGE,
    val permSet: PermissionSet = PermissionSet.of(
        Permission.READ_MESSAGE_HISTORY,
        Permission.SEND_MESSAGES
    )
) {

    companion object {
        const val REQUIRE_BOT_OWNER = false
        const val REQUIRE_GUILD_OWNER = false
        const val REQUIRE_BOT_MENTION = false
        const val ALLOW_DIRECT_MESSAGING = true
        const val REQUIRE_DIRECT_MESSAGING = false
        const val REMOVE_INVOCATION_MESSAGE = false
    }
}
