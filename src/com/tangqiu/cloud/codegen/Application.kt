package com.tangqiu.cloud.codegen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tangqiu.cloud.codegen.client.GitClient
import com.tangqiu.cloud.codegen.client.gitClient
import com.tangqiu.cloud.codegen.entity.FlutterGenConfig
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.utils.io.*
import java.security.SecureRandom
import javax.net.ssl.SSLContext

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = "/ktor/application/shutdown"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }
    //跨域
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
    install(ContentNegotiation) {
        gson { }
    }
    install(Locations)
    install(GitClient)

    val fileClient = HttpClient(Apache) {
        engine {
            //sslContext = SSLContext.getInstance("TLS")
            //    .apply {
            //        init(null, arrayOf(GitClient.TrustAllX509TrustManager()), SecureRandom())
            //    }
        }
    }

    routing {
        get("/api/v4/{path...}") {
            val authorization = call.request.header("Authorization") ?: ""
            call.respond(gitClient.get<JsonArray>(call.request.uri) {
                headers { append("Authorization", authorization) }
            })
        }
        post("/oauth/token") {
            call.respond(gitClient.post<JsonObject>(call.request.uri) {
                contentType(ContentType.Application.Json)
                body = call.receive<JsonObject>()
            })
        }
        post("/generate/{lang}") {
            val lang = call.parameters["lang"] ?: throw IllegalArgumentException("lang must be set")
            when (lang) {
                "flutter" -> {
                    val config = call.receive<FlutterGenConfig>()
                    call.response.header(
                        "Content-Disposition",
                        "attachment;filename=${config.pubName}_${config.pubVersion}.zip"
                    )
                    call.generateFlutter(config) {
                        call.respondFile(it)
                    }
                }
                else -> throw IllegalArgumentException("lang must be flutter")
            }
        }
        get("/file") {
            val path = call.request.queryParameters["path"] ?: throw IllegalArgumentException()
            fileClient.get<HttpStatement>(path).execute { response: HttpResponse ->
                // Response is not downloaded here.
                val channel = response.receive<ByteReadChannel>()
                call.respondBytesWriter {
                    channel.copyTo(this) //响应二进制流
                }
            }
        }
        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/") {
            resources("static")
            defaultResource("static/index.html")
        }
    }
}

