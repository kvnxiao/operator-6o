package com.github.kvnxiao.discord.commands.search

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kvnxiao.discord.command.annotation.Descriptor
import com.github.kvnxiao.discord.command.annotation.Id
import com.github.kvnxiao.discord.command.context.Context
import com.github.kvnxiao.discord.command.executable.Command
import com.github.kvnxiao.discord.http.HttpRequest
import com.github.kvnxiao.discord.http.HttpResponseHandler
import discord4j.core.`object`.entity.Message
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom
import org.apache.http.client.utils.URIBuilder
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse

@Id("xkcd")
@Descriptor(
    description = "Displays a random post from XKCD, or the latest post from XKCD with `latest`.",
    usage = "%A | %A `latest`"
)
class XkcdCommand : Command, HttpResponseHandler {
    companion object {
        private val XKCD_API_URL_BUILDER: URIBuilder
            get() = URIBuilder()
                .setScheme("https")
                .setHost("xkcd.com")
        private const val DEFAULT_PATH = "info.0.json"
        private val EMBED_COLOR = Color(150, 168, 200)
        private fun client(path: String = DEFAULT_PATH): HttpClient.ResponseReceiver<*> =
            HttpClient.create()
                .get()
                .uri(XKCD_API_URL_BUILDER.setPath(path).build().toASCIIString())
    }

    data class XKCDResponse(
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

    override fun execute(ctx: Context): Mono<Void> =
        Mono.justOrEmpty(ctx.args.arguments)
            .filter { it == "latest" }
            .flatMap { getLatestPost(ctx) }
            .switchIfEmpty(getRandomPost(ctx))
            .then()

    override fun handleInputStream(ctx: Context, body: ByteBufMono): Mono<Message> =
        body.asInputStream()
            .map { HttpRequest.OBJECT_MAPPER.readValue<XKCDResponse>(it) }
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

    override fun handleError(ctx: Context, response: HttpClientResponse): Mono<Message> =
        ctx.channel.createMessage("Unable to query XKCD:\n${response.status().code()} - ${response.status().reasonPhrase()}")

    private fun getNumber(body: ByteBufMono): Mono<Int> =
        body.asInputStream()
            .map { HttpRequest.OBJECT_MAPPER.readValue<XKCDResponse>(it) }
            .map { response -> response.num }

    private fun getLatestPost(ctx: Context): Mono<Message> =
        client().responseSingle { response, body -> handleResponse(ctx, response, body) }

    private fun getRandomPost(ctx: Context): Mono<Message> =
        client().responseSingle { response, body ->
            if (response.status().code() in 200..299) {
                getNumber(body)
                    .flatMap { latestNum ->
                        client("${ThreadLocalRandom.current().nextInt(latestNum)}/$DEFAULT_PATH")
                            .responseSingle { response, body -> handleResponse(ctx, response, body) }
                    }
            } else {
                handleError(ctx, response)
            }
        }
}
