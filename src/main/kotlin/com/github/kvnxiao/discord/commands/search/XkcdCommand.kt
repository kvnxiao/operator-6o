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
package com.github.kvnxiao.discord.commands.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kvnxiao.discord.command.annotation.Alias
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.annotation.SubCommand
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.http.HttpResponseHandler
import com.github.kvnxiao.discord.http.isSuccessCode
import discord4j.core.`object`.entity.Message
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom
import org.apache.http.client.utils.URIBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse

private data class XKCDResponse(
    val month: String,
    val num: Int,
    val link: String,
    val year: String,
    val news: String,
    val safe_title: String,
    val transcript: String,
    val alt: String,
    val img: String,
    val title: String,
    val day: String
)

private object Xkcd {
    val XKCD_API_URL_BUILDER: URIBuilder
        get() = URIBuilder()
            .setScheme("https")
            .setHost("xkcd.com")

    const val DEFAULT_PATH = "info.0.json"

    val EMBED_COLOR = Color(150, 168, 200)

    fun client(path: String = DEFAULT_PATH): HttpClient.ResponseReceiver<*> =
        HttpClient.create()
            .get()
            .uri(XKCD_API_URL_BUILDER.setPath(path).build().toASCIIString())

    fun handleInputStream(ctx: Context, body: ByteBufMono, objectMapper: ObjectMapper): Mono<Message> =
        body.asInputStream()
            .map { objectMapper.readValue<XKCDResponse>(it) }
            .flatMap { response ->
                ctx.channel.createMessage { spec ->
                    spec.setEmbed { embedSpec ->
                        embedSpec
                            .setTitle("${response.title} (#${response.num})")
                            .setDescription(response.alt)
                            .setImage(response.img)
                            .setFooter(
                                "xkcd #${response.num} | ${response.year}-${response.month}-${response.day}",
                                null
                            )
                            .setColor(EMBED_COLOR)
                    }
                }
            }

    fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        ctx.channel.createMessage(
            "Unable to query XKCD:\n${response.status().code()} - ${response.status().reasonPhrase()}"
        )
}

@Component
@Id("xkcd")
@Descriptor(
    description = "Displays a random post from XKCD, or the latest post from XKCD with `latest`.",
    usage = "%A | %A <post number> | %A `latest`"
)
@SubCommand([XkcdLatestCommand::class])
class XkcdCommand(
    private val objectMapper: ObjectMapper
) : Command, HttpResponseHandler {
    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .map { it.toInt() }
            .filter { it > 0 }
            .flatMap { getPost(ctx, it) }
            .switchIfEmpty(getRandomPost(ctx))
            .then()
            .onErrorResume { Mono.empty() }

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        Xkcd.handleInputStream(ctx, body, objectMapper)

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        Xkcd.handleError(ctx, response)

    private fun getNumber(body: ByteBufMono): Mono<Int> =
        body.asInputStream()
            .map { objectMapper.readValue<XKCDResponse>(it) }
            .map { response -> response.num }

    private fun getPost(ctx: Context, postNumber: Number): Mono<Message> =
        Xkcd.client("$postNumber/${Xkcd.DEFAULT_PATH}").responseSingle { response, body ->
            handleResponse(
                ctx,
                response,
                body
            )
        }

    private fun getRandomPost(ctx: Context): Mono<Message> =
        Xkcd.client().responseSingle { response, body ->
            if (response.isSuccessCode()) {
                getNumber(body)
            } else {
                Mono.empty()
            }
        }.flatMap { latestNum ->
            getPost(ctx, ThreadLocalRandom.current().nextInt(1, latestNum + 1))
        }
}

@Component
@Id("xkcd.latest")
@Alias(["latest"])
@Descriptor(
    description = "Displays the latest post from XKCD with `latest`.",
    usage = "%A"
)
class XkcdLatestCommand(
    private val objectMapper: ObjectMapper
) : Command, HttpResponseHandler {
    override fun execute(ctx: Context): Mono<Void> =
        getLatestPost(ctx)
            .then()
            .onErrorResume { Mono.empty() }

    private fun getLatestPost(ctx: Context): Mono<Message> =
        Xkcd.client().responseSingle { response, body -> handleResponse(ctx, response, body) }

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        Xkcd.handleInputStream(ctx, body, objectMapper)

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        Xkcd.handleError(ctx, response)
}
