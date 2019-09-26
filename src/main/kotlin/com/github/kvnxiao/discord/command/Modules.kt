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
package com.github.kvnxiao.discord.command

import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.registry.MapTreeRegistryRoot
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.validation.Validator
import com.github.kvnxiao.discord.command.validation.context.ContextValidator
import com.github.kvnxiao.discord.command.validation.context.PermissionValidator
import com.github.kvnxiao.discord.command.validation.message.ChannelValidator
import com.github.kvnxiao.discord.command.validation.message.SourceValidator
import com.github.kvnxiao.discord.env.Environment
import discord4j.core.`object`.entity.Message
import org.koin.core.qualifier.named
import org.koin.dsl.module

object Modules {
    val validationModule = module {
        single<Validator<Context>>(named("context")) { ContextValidator() }
        single<Validator<Context>>(named("permission")) { PermissionValidator() }
        single<Validator<Message>>(named("source")) { SourceValidator() }
        single<Validator<Message>>(named("channel")) { ChannelValidator() }
    }
    val environmentModule = module {
        single<String>(named(Environment.TOKEN)) { getProperty(Environment.TOKEN) }
    }
    val commandModule = module {
        single<RegistryNode> { MapTreeRegistryRoot() }
        single { CommandProcessor(getAll(), getAll(), get()) }
    }
}
