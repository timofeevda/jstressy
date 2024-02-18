package com.github.timofeevda.jstressy.metrics.scrapper

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.config.parameters.StressyRenderedMetrics
import com.github.timofeevda.jstressy.api.httpclient.HttpClientService
import com.github.timofeevda.jstressy.utils.Duration
import com.github.timofeevda.jstressy.utils.StressyUtils
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.reactivex.Flowable
import io.reactivex.disposables.Disposables
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.HttpClient
import java.io.File
import java.time.Instant.ofEpochMilli
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private data class Image(val buffer: Buffer, val from: Long, val to: Long)

open class RenderedMetricsScrapperService(
    private val configurationService: ConfigurationService,
    private val httpClientService: HttpClientService
) {

    companion object : LazyLogging()

    fun start() {
        val startTime = System.currentTimeMillis()
        val renderedMetrics = configurationService.configuration.globals.renderedMetrics

        if (renderedMetrics.isEmpty()) {
            logger.info("Rendered metrics set is not configured. Skipping rendered metrics scrapping")
        } else {
            renderedMetrics.forEach {
                val scrapper = RenderedMetricsScrapper(it, httpClientService.get(), startTime)
                scrapper.run()
            }
        }
    }
}

private class RenderedMetricsScrapper(
    private val renderedMetrics: StressyRenderedMetrics,
    private val httpClient: HttpClient,
    private val startTime: Long
) {
    companion object : LazyLogging()

    private var disposable = Disposables.empty()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(renderedMetrics.dateTimeFormat)
        .withZone(renderedMetrics.timeZone?.let { zone -> ZoneId.of(zone) } ?: ZoneId.systemDefault())

    fun run() {
        val folder = File(renderedMetrics.folder)

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val period = StressyUtils.parseDuration(renderedMetrics.period)

        disposable = Flowable.interval(period.toMilliseconds(), TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .flatMap {
                downloadImage(period)
            }
            .subscribe({
                saveImage(it)
            }, {
                logger.error("Error scheduling rendered metrics download $renderedMetrics", it)
            })

    }

    private fun downloadImage(period: Duration): Flowable<Image> {
        val to = System.currentTimeMillis()
        val from = if (renderedMetrics.cumulative) startTime else to - period.toMilliseconds();
        val fromParameter = renderedMetrics.fromParameter ?: "from"
        val toParameter = renderedMetrics.toParameter ?: "to"
        val uri =
            renderedMetrics.uri + if (renderedMetrics.uri.contains("?")) "&$fromParameter=$from&$toParameter=$to" else "?$fromParameter=$from&$toParameter=$to"

        return httpClient.rxRequest(
            RequestOptions()
                .setMethod(HttpMethod.GET)
                .setPort(renderedMetrics.port)
                .setHost(renderedMetrics.host)
                .setSsl(renderedMetrics.useSsl)
                .setURI(uri)
        )
            .flatMap { r -> r.rxConnect() }
            .flatMap { r ->
                r.rxBody().map {
                    Image(it, from, to)
                }
            }
            .doOnSubscribe {
                logger.info("Going to request rendered metric for $renderedMetrics with URI: $uri")
            }.toFlowable()
    }

    private fun saveImage(image: Image) {
        val fromDate = dateTimeFormatter.format(ofEpochMilli(image.from))
        val toDate = dateTimeFormatter.format(ofEpochMilli(image.to))

        val imageFile =
            File(renderedMetrics.folder + File.separator + renderedMetrics.filePrefix + "$fromDate-$toDate" + renderedMetrics.fileExtension)
        imageFile.writeBytes(image.buffer.bytes)
    }

    fun stop() {
        disposable.dispose()
    }
}