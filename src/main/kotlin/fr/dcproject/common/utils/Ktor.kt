package fr.dcproject.common.utils

import io.ktor.application.Application
import io.ktor.application.ApplicationStopped

fun Application.onApplicationStopped(callback: Application.() -> Unit) {
    environment.monitor.subscribe(ApplicationStopped) {
        callback()
    }
}
