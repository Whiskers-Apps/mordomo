package org.whiskersapps.mordomo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform