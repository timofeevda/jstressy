package com.github.timofeevda.jstressy.utils.logging

interface LazyLogger {
    fun trace(msg: String?)
    fun debug(msg: String?)
    fun info(msg: String?)
    fun warn(msg: String?)
    fun error(msg: String?)

    fun trace(msg: () -> Any?)
    fun debug(msg: () -> Any?)
    fun info(msg: () -> Any?)
    fun warn(msg: () -> Any?)
    fun error(msg: () -> Any?)

    fun trace(msg: () -> Any?, t: Throwable?)
    fun debug(msg: () -> Any?, t: Throwable?)
    fun info(msg: () -> Any?, t: Throwable?)
    fun warn(msg: () -> Any?, t: Throwable?)
    fun error(msg: () -> Any?, t: Throwable?)

    fun trace(msg: String?, t: Throwable?)
    fun debug(msg: String?, t: Throwable?)
    fun info(msg: String?, t: Throwable?)
    fun warn(msg: String?, t: Throwable?)
    fun error(msg: String?, t: Throwable?)
}

open class LazyLogging {
    val logger: LazyLogger = LazyLoggerFactory.logger(this)

    companion object {
        fun logger(func: () -> Unit): LazyLogger = LazyLoggerFactory.logger(func)
    }
}