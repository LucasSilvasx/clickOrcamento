package com.clickorcamento.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform