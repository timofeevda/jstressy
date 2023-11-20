package com.github.timofeevda.jstressy.api.scenario

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioContext
import io.reactivex.disposables.Disposable

/**
 * Lightweight interface with limited access to scenario instance
 */
interface ScenarioHandle {
    /**
     * Get access to scenario context map
     */
    fun ctx() : ScenarioContext

    /**
     * Add disposable to scenario handle. Can be used primarily in DSL to dispose subscriptions once stop method is invoked
     */
    fun addDisposable(disposable: Disposable)

    /**
     * Stop scenario and invoke clean-up actions if any
     */
    fun stop(): Unit
}