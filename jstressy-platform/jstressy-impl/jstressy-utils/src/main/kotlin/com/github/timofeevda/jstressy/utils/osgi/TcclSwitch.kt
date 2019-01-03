/**
 * See https://github.com/vert-x3/vertx-examples/blob/master/osgi-examples/src/main/java/io/vertx/example/osgi/TcclSwitch.java
 */
package com.github.timofeevda.jstressy.utils.osgi

import java.util.concurrent.Callable

object TcclSwitch {

    @Throws(Exception::class)
    fun <T> executeWithTCCLSwitch(action: Callable<T>): T {
        val original = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = TcclSwitch::class.java.classLoader
            return action.call()
        } finally {
            Thread.currentThread().contextClassLoader = original
        }
    }

    fun executeWithTCCLSwitch(action: Runnable) {
        val original = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = TcclSwitch::class.java.classLoader
            action.run()
        } finally {
            Thread.currentThread().contextClassLoader = original
        }
    }

}