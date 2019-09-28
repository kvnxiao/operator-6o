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
package com.github.kvnxiao.discord.command.annotation

import com.github.kvnxiao.discord.command.permission.Permissions
import discord4j.core.`object`.util.Permission

annotation class Permissions(
    val requireBotOwner: Boolean = Permissions.REQUIRE_BOT_OWNER,
    val requireGuildOwner: Boolean = Permissions.REQUIRE_GUILD_OWNER,
    val requireBotMention: Boolean = Permissions.REQUIRE_BOT_MENTION,
    val allowDirectMessaging: Boolean = Permissions.ALLOW_DIRECT_MESSAGING,
    val requireDirectMessaging: Boolean = Permissions.REQUIRE_DIRECT_MESSAGING,
    val removeInvocationMessage: Boolean = Permissions.REMOVE_INVOCATION_MESSAGE,
    val permSet: Array<Permission> = [Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES]
)
