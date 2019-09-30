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
package com.github.kvnxiao.discord.koin

import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.executable.CommandExecutable
import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.processor.AnnotationProcessor
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.registry.MapTreeRegistryRoot
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.validation.context.ContextValidator
import com.github.kvnxiao.discord.command.validation.context.MessageContextValidator
import com.github.kvnxiao.discord.command.validation.context.PermissionValidator
import com.github.kvnxiao.discord.command.validation.message.ChannelValidator
import com.github.kvnxiao.discord.command.validation.message.MessageValidator
import com.github.kvnxiao.discord.command.validation.message.SourceValidator
import com.github.kvnxiao.discord.commands.PingCommand
import org.koin.core.KoinComponent
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object Modules {
    val validationModule = module {
        single<ContextValidator>(named("context")) { MessageContextValidator() }
        single<ContextValidator>(named("permission")) { PermissionValidator() }
        single<MessageValidator>(named("source")) { SourceValidator() }
        single<MessageValidator>(named("channel")) { ChannelValidator() }
    }
    val commandProcessingModule = module {
        single { PrefixSettings() }
        single<RegistryNode> { MapTreeRegistryRoot() }
        single { CommandProcessor(getAll(), getAll(), get(), get()) }
        single { AnnotationProcessor() }
    }
    val commandsModule = module {
        command(PingCommand())
    }
}

private fun Module.command(commandExecutable: CommandExecutable) {
    val annotations = commandExecutable::class.annotations
    val id = annotations.find { an -> an is Id } as? Id
    require(id != null) { "@Id annotation must exist for command ${commandExecutable::class}" }
    this.single(named(id.id)) { commandExecutable }
}

fun KoinComponent.getProperty(key: String): String = this.getKoin().getProperty<String>(key)
    ?: throw IllegalArgumentException("Missing value for environment variable: $key.")

inline fun <reified T> KoinComponent.getAll(): List<T> = this.getKoin().getAll()
