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
package com.github.kvnxiao.discord.client

import com.github.kvnxiao.discord.command.prefix.PrefixSettings
import com.github.kvnxiao.discord.command.prefix.RedisPrefixSettings
import com.github.kvnxiao.discord.command.processor.AnnotationProcessor
import com.github.kvnxiao.discord.command.processor.CommandProcessor
import com.github.kvnxiao.discord.command.registry.MapTreeRegistryRoot
import com.github.kvnxiao.discord.command.registry.PropertiesRegistry
import com.github.kvnxiao.discord.command.registry.RegistryNode
import com.github.kvnxiao.discord.command.validation.context.ContextValidator
import com.github.kvnxiao.discord.command.validation.context.MessageContextValidator
import com.github.kvnxiao.discord.command.validation.context.PermissionValidator
import com.github.kvnxiao.discord.command.validation.message.ChannelValidator
import com.github.kvnxiao.discord.command.validation.message.MessageValidator
import com.github.kvnxiao.discord.command.validation.message.SourceValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisTemplate

@Configuration
class CommandConfiguration {
    @Bean
    fun prefixSettings(redis: ReactiveRedisTemplate<String, String>): PrefixSettings =
        RedisPrefixSettings(redis)

    @Bean
    fun rootRegistry(): RegistryNode =
        MapTreeRegistryRoot()

    @Bean
    fun annotationProcessor(): AnnotationProcessor =
        AnnotationProcessor()

    @Bean
    fun messageValidators(): List<MessageValidator> =
        listOf(
            ChannelValidator(),
            SourceValidator()
        )

    @Bean
    fun contextValidators(): List<ContextValidator> =
        listOf(
            MessageContextValidator(),
            PermissionValidator()
        )

    @Bean
    fun commandPropertiesRegistry(registryRoot: RegistryNode): PropertiesRegistry =
        PropertiesRegistry(registryRoot)

    @Bean
    fun commandProcessor(
        messageValidators: List<MessageValidator>,
        contextValidators: List<ContextValidator>,
        registryRoot: RegistryNode,
        prefixSettings: PrefixSettings
    ): CommandProcessor =
        CommandProcessor(messageValidators, contextValidators, registryRoot, prefixSettings)
}
