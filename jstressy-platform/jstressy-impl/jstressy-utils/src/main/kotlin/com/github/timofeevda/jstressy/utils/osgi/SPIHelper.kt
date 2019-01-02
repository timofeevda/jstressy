/**
 * See gist https://gist.github.com/cescoffier/e8aec18cc86581923f1cc639c2a71c4d
 */
package com.github.timofeevda.jstressy.utils.osgi

import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * A class loading SPI implementation for Vert.x
 */
object SPIHelper {

    private val logger = LazyLogging().logger

    @Throws(IOException::class)
    fun <T> lookup(clazz: Class<T>, bc: BundleContext, hint: String): T? {
        return bc.bundles
                .filter { it.bundleId != 0L }
                .map { loadAndInstantiate(clazz, hint, it) }
                .find { it != null }
    }

    @Throws(IOException::class)
    private fun <T> loadAndInstantiate(clazz: Class<T>, hint: String?, bundle: Bundle): T? {
        val url = bundle.getResource("META-INF/services/" + clazz.name)
        return if (url != null) {
            val stream = url.openStream()
            BufferedReader(InputStreamReader(stream)).useLines {
                var className: String? = null
                val classLine = it.find { line -> !line.startsWith("#") && !line.trim { c -> c <= ' ' }.isEmpty() }
                if (classLine != null) {
                    className = classLine.trim { c -> c <= ' ' }
                }
                return instantiateForClassName(bundle, className, hint)
            }
        } else {
            null
        }
    }

    private fun <T> instantiateForClassName(bundle: Bundle, className: String?, hint: String?): T? {
        if (className == null) {
            return null
        }

        return if (hint != null) {
            if (className.toLowerCase().contains(hint.toLowerCase())) {
                instantiate<T>(bundle, className)
            } else null
        } else {
            instantiate<T>(bundle, className)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> instantiate(bundle: Bundle, className: String): T? {
        try {
            val clazz = bundle.loadClass(className) as Class<T>
            return clazz.newInstance()
        } catch (e: ClassNotFoundException) {
            logger.error("Cannot load class " + className + " from " + bundle.symbolicName)
        } catch (e: InstantiationException) {
            logger.error("Cannot instantiate class " + className + " from " + bundle.symbolicName, e)
        } catch (e: IllegalAccessException) {
            logger.error("Cannot instantiate class " + className + " from " + bundle.symbolicName, e)
        }
        return null
    }

}