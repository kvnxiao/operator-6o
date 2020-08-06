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
package com.github.kvnxiao.discord.commands.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.d4j.embed
import com.github.kvnxiao.discord.env.Environment
import com.github.kvnxiao.discord.http.HttpResponseHandler
import com.github.kvnxiao.discord.reaction.ReactionUnicode
import discord4j.core.`object`.entity.Message
import discord4j.rest.util.Color
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse

@JsonIgnoreProperties(ignoreUnknown = true)
private data class WolframAlphaResult(
    val queryresult: QueryResult
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class QueryResult(
    val success: Boolean,
    val numpods: Int,
    val timing: Float,
    val parsetiming: Float,
    val version: String,
    val pods: List<Pod>
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class SubPod(
    val title: String,
    val plaintext: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Pod(
    val title: String,
    val scanner: String,
    val id: String,
    val position: Int,
    val numsubpods: Int,
    val subpods: List<SubPod>
)

@Component
@Id("wolframalpha")
@Alias(["wa", "wolframalpha"])
@Descriptor(
    description = "Searches on WolframAlpha.",
    usage = "%A <query>"
)
class WolframAlphaCommand(
    @Value(Environment.WOLFRAM_ALPHA_APP_ID) private val appId: String,
    private val objectMapper: ObjectMapper
) : Command, HttpResponseHandler {

    companion object {
        private const val THUMBNAIL = "https://www.wolframalpha.com/_next/static/images/share_3G6HuGr6.png"
        private val EMBED_COLOR = Color.of(0xfd745c)
        private val CUSTOM_SEARCH_URL_BUILDER: URIBuilder
            get() = URIBuilder()
                .setScheme("https")
                .setHost("api.wolframalpha.com/")
                .setPath("v2/query")
                .setParameter("output", "json")
    }

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .map { query ->
                CUSTOM_SEARCH_URL_BUILDER
                    .addParameter("appid", appId)
                    .addParameter("input", query)
                    .build()
                    .toASCIIString()
            }
            .map { uri -> HttpClient.create().get().uri(uri) }
            .flatMap { req ->
                req.responseSingle { response, body ->
                    handleResponse(ctx, response, body)
                }
            }.then()

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        body.asInputStream()
            .map { objectMapper.readValue<WolframAlphaResult>(it) }
            .map { it.queryresult }
            .flatMap { result ->
                ctx.channel.createEmbed(
                    embed {
                        setTitle("${ReactionUnicode.MAG_RIGHT} WolframAlpha")
                        setThumbnail(THUMBNAIL)
                        setColor(EMBED_COLOR)
                        setFooter("Query took ${result.timing}s", null)

                        if (result.success) {
                            result.pods.forEach { pod ->
                                val subpods = pod.subpods.filter { it.plaintext.isNotBlank() }
                                if (subpods.isNotEmpty()) {
                                    addField(
                                        pod.title,
                                        subpods.joinToString(separator = "\n") { subPod -> subPod.plaintext },
                                        false
                                    )
                                }
                            }
                        } else {
                            setDescription("Failed to query `${ctx.args.arguments}` on WolframAlpha.")
                        }
                    }
                )
            }

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        ctx.channel.createMessage(
            "An error occurred while searching for ${ctx.args.arguments} on WolframAlpha.\n${response.status()
                .code()} - ${response.status().reasonPhrase()}"
        )
}
