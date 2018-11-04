package com.github.timofeevda.jstressy.utils

import io.reactivex.Single
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceEvent

object StressyUtils {

    @Suppress("UNCHECKED_CAST")
    fun <T> observeService(className: String, bundleContext: BundleContext): Single<T> {
        return Single.create { singleEmitter ->
            val serviceListener = { event: ServiceEvent ->
                val ref = event.serviceReference
                if (event.type == ServiceEvent.REGISTERED) {
                    singleEmitter.onSuccess(bundleContext.getService(ref) as T)
                }
            }
            bundleContext.addServiceListener(serviceListener, "(objectClass=$className)")
            synchronized(singleEmitter) {
                val ref = bundleContext.getServiceReference(className)
                if (ref != null) {
                    singleEmitter.onSuccess(bundleContext.getService(ref) as T)
                }
            }
            singleEmitter.setCancellable { bundleContext.removeServiceListener(serviceListener) }
        }
    }
}