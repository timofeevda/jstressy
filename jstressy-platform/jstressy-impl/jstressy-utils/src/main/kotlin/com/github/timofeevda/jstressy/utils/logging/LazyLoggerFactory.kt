package com.github.timofeevda.jstressy.utils.logging

import org.slf4j.LoggerFactory

object LazyLoggerFactory {

    internal fun logger(o: Any): LazyLogger =
            logger(getClassForLogging(o.javaClass).name)

    private fun <T : Any> getClassForLogging(clazz: Class<T>): Class<*> {
        if (clazz.kotlin.isCompanion) {
            return clazz.enclosingClass ?: clazz
        }
        return clazz
    }

    private fun logger(name: String): LazyLogger = LazyLoggerImpl(LoggerFactory.getLogger(name))
}