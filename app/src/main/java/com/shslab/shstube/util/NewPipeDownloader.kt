package com.shslab.shstube.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.TimeUnit

/**
 * Minimal OkHttp-backed Downloader implementation for NewPipeExtractor.
 * Mirrors the pattern used by the official NewPipe Android client.
 */
class NewPipeDownloader private constructor(private val client: OkHttpClient) : Downloader() {

    override fun execute(request: org.schabi.newpipe.extractor.downloader.Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val builder = Request.Builder()
            .method(
                httpMethod,
                if (dataToSend != null) dataToSend.toRequestBody(null, 0, dataToSend.size) else null
            )
            .url(url)
            .addHeader("User-Agent", USER_AGENT)

        for ((name, values) in headers) {
            builder.removeHeader(name)
            for (v in values) builder.addHeader(name, v)
        }

        val resp = client.newCall(builder.build()).execute()
        val bodyStr = resp.body?.string() ?: ""
        val multimap = HashMap<String, List<String>>()
        for (name in resp.headers.names()) {
            multimap[name] = resp.headers.values(name)
        }
        return Response(resp.code, resp.message, multimap, bodyStr, resp.request.url.toString())
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Mobile Safari/537.36 SHSTube/2.1"

        fun create(): NewPipeDownloader {
            val client = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
            return NewPipeDownloader(client)
        }
    }
}
