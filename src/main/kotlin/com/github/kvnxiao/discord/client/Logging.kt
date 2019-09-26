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
package com.github.kvnxiao.discord.client

import mu.KLogger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

inline fun <T> Flux<T>.debug(logger: KLogger, crossinline msg: (T) -> Any?): Flux<T> =
    this.doOnNext { logger.debug { msg(it) } }

inline fun <T> Flux<T>.info(logger: KLogger, crossinline msg: (T) -> Any?): Flux<T> =
    this.doOnNext { logger.info { msg(it) } }

inline fun <T> Flux<T>.error(logger: KLogger, crossinline msg: (T) -> Any?): Flux<T> =
    this.doOnNext { logger.error { msg(it) } }

inline fun <T> Flux<T>.warn(logger: KLogger, crossinline msg: (T) -> Any?): Flux<T> =
    this.doOnNext { logger.warn { msg(it) } }

inline fun <T> Flux<T>.trace(logger: KLogger, crossinline msg: (T) -> Any?): Flux<T> =
    this.doOnNext { logger.trace { msg(it) } }

inline fun <T> Mono<T>.debug(logger: KLogger, crossinline msg: (T) -> Any?): Mono<T> =
    this.doOnNext { logger.debug { msg(it) } }

inline fun <T> Mono<T>.info(logger: KLogger, crossinline msg: (T) -> Any?): Mono<T> =
    this.doOnNext { logger.info { msg(it) } }

inline fun <T> Mono<T>.error(logger: KLogger, crossinline msg: (T) -> Any?): Mono<T> =
    this.doOnNext { logger.error { msg(it) } }

inline fun <T> Mono<T>.warn(logger: KLogger, crossinline msg: (T) -> Any?): Mono<T> =
    this.doOnNext { logger.warn { msg(it) } }

inline fun <T> Mono<T>.trace(logger: KLogger, crossinline msg: (T) -> Any?): Mono<T> =
    this.doOnNext { logger.trace { msg(it) } }
