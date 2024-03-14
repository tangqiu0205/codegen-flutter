package com.tangqiu.cloud.codegen.client

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class GitClient(
    internal val config: Config
) {
    val client = HttpClient(Apache) {
        engine {
            sslContext = SSLContext.getInstance("TLS")
                .apply {
                    init(null, arrayOf(TrustAllX509TrustManager()), SecureRandom())
                }
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    data class Config(
        var protocol: URLProtocol = URLProtocol.HTTP,
        var host: String = "xxx.xxx.com", //Gitlab地址
        var port: Int = 29876 //端口号
    )

    class TrustAllX509TrustManager : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)

        override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}

        override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
    }

    companion object Feature : ApplicationFeature<Application, Config, GitClient> {
        override val key: AttributeKey<GitClient> = AttributeKey("GitClient")

        override fun install(pipeline: Application, configure: Config.() -> Unit): GitClient {
            val config = Config().apply(configure)
            return GitClient(config).apply {
                client.requestPipeline.intercept(HttpRequestPipeline.Before) {
                    context.url {
                        protocol = config.protocol
                        host = config.host
                        port = config.port
                        path(encodedPath.removeSurrounding("/"))
                    }
                    proceed()
                }
            }
        }
    }
}

val Application.gitClient get() = feature(GitClient).client
