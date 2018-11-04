package com.github.timofeevda.jstressy.utils.logging

import org.slf4j.Logger

internal class LazyLoggerImpl(private val jLogger: Logger) : LazyLogger {

    override fun trace(msg: String?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg)
    }

    override fun debug(msg: String?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg)
    }

    override fun info(msg: String?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg)
    }

    override fun warn(msg: String?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg)
    }

    override fun error(msg: String?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg)
    }

    override fun trace(msg: () -> Any?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString())
    }

    override fun debug(msg: () -> Any?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString())
    }

    override fun info(msg: () -> Any?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString())
    }

    override fun warn(msg: () -> Any?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString())
    }

    override fun error(msg: () -> Any?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString())
    }

    override fun trace(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString(), t)
    }

    override fun debug(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString(), t)
    }

    override fun info(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString(), t)
    }

    override fun warn(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString(), t)
    }

    override fun error(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString(), t)
    }

    override fun trace(msg: String?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg, t)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun debug(msg: String?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg, t)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun info(msg: String?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg, t)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun warn(msg: String?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg, t)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun error(msg: String?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg, t)
    }

    private fun (() -> Any?).toSafeString(): String {
        return try {
            invoke().toString()
        } catch (e: Exception) {
            "Exception while invokin log message: $e"
        }
    }
}