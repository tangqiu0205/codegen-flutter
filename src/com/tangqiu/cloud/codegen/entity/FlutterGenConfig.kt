package com.tangqiu.cloud.codegen.entity

data class FlutterGenConfig(
    val url: String,
    val pubName: String,
    val pubVersion: String,
    val pubClassName: String? = null,
    val apiLibHost: String? = null,
    val apiLibVersion: String? = null,
    val accessToken: String? = null,
    val basePath: String? = null,
)