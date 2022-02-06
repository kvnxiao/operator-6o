/**
 *   Copyright 2020 Ze Hao (Kevin) Xiao
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

import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.Permissions
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.d4j.embed
import discord4j.common.GitProperties.GIT_COMMIT_ID_DESCRIBE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.info.GitProperties
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

@Component
@Id("version")
@Descriptor(
    description = "Shows the build and version info for the bot.",
    usage = "%A"
)
@Permissions(allowDirectMessaging = true)
class VersionCommand(
    gitProperties: GitProperties,
    @Qualifier("discord4jProperties") discord4jProperties: Properties
) : Command {

    companion object {
        private const val REPO_LINK = "https://github.com/kvnxiao/operator-6o/commit/"
        private const val BUILD_VERSION = "build.version"
        private const val COMMIT_TIME = "commit.time"
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
    }

    private val shortCommitId: String = gitProperties.shortCommitId
    private val buildVersion: String = gitProperties[BUILD_VERSION]
    private val commitTime: String = ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(gitProperties[COMMIT_TIME].toLong()),
        ZoneId.systemDefault()
    ).format(DATE_FORMAT)
    private val discord4jVersion: String = discord4jProperties.getProperty(GIT_COMMIT_ID_DESCRIBE)

    override fun execute(ctx: Context): Mono<Void> =
        ctx.channel.createMessage(
            embed {
                title("Version Info")
                addField("Commit ID", "[$shortCommitId]($REPO_LINK$shortCommitId)", true)
                addField("Commit Time", commitTime, true)
                addField("Version", buildVersion, false)
                addField("Discord4J Version", discord4jVersion, false)
            }
        ).then()
}
