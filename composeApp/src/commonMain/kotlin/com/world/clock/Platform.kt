package com.world.clock

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform