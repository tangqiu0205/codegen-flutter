package com.tangqiu.cloud.codegen

import com.tangqiu.cloud.codegen.entity.FlutterGenConfig
import com.tangqiu.cloud.codegen.util.FileZipUtil
import io.ktor.application.*
import io.swagger.codegen.v3.DefaultGenerator
import io.swagger.codegen.v3.config.CodegenConfigurator
import io.swagger.v3.parser.core.models.AuthorizationValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 通过OpenApi json 生成 Flutter代码
 */
suspend fun ApplicationCall.generateFlutter(
    config: FlutterGenConfig,
    block: suspend (File) -> Unit
) {
    //代码生成配置
    val configurator = CodegenConfigurator()
    configurator.lang = FlutterClientCodegen::class.java.name
    configurator.templateDir = "/"
    configurator.outputDir = "resources/generate/${Date().time}" //输出目录

    configurator.inputSpecURL = config.url //url添加token
    configurator.additionalProperties[FlutterClientCodegen.PUB_NAME] = config.pubName
    configurator.additionalProperties[FlutterClientCodegen.PUB_VERSION] = config.pubVersion
    configurator.additionalProperties[FlutterClientCodegen.PUB_CLASS_NAME] = config.pubClassName
    configurator.additionalProperties[FlutterClientCodegen.API_LIB_HOST] = config.apiLibHost
    configurator.additionalProperties[FlutterClientCodegen.API_LIB_VERSION] = config.apiLibVersion
    configurator.additionalProperties[FlutterClientCodegen.BASE_PATH] = config.basePath
    configurator.authorizationValue = AuthorizationValue(
        "access_token", config.accessToken, "header"
    )

    val clientOptInput = configurator.toClientOptInput()

    withContext(Dispatchers.IO) {
        DefaultGenerator().opts(clientOptInput).generate()
        val zip = File.createTempFile("api_gen_", "") //创建临时文件
        FileZipUtil.fileToZip(configurator.outputDir, zip.outputStream()) //文件压缩
        block.invoke(zip)
        zip.delete()
    }
}
