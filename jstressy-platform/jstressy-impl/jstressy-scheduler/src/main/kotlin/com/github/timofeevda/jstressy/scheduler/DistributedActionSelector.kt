package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.disposables.Disposables

private object EmptyEmitter : Emitter<ScenarioAction> {
    override fun onNext(value: ScenarioAction) {
        // do nothing
    }

    override fun onError(error: Throwable) {
        // do nothing
    }

    override fun onComplete() {
        // do nothing
    }

}
data class ScenarioActionReceiver(val scenario: Scenario, val emitter: Emitter<ScenarioAction>)

data class ScenarioActionExecutor(val scenarioAction: ScenarioAction, val emitter: Emitter<ScenarioAction>)

object NoOpAction : ScenarioAction {
    override fun run() {
        // do nothing
    }
}

interface DistributedActionSelector {

    fun observeActions(scenario: Scenario): Observable<ScenarioAction>

}

abstract class AbstractDistributedActionSelector(
    private val actionDefinition: ScenarioActionDefinition,
    private val observeScenarioActionArrivals: Observable<String>
) : DistributedActionSelector {

    companion object : LazyLogging()

    @Volatile
    protected var isSubscribed: Boolean = false

    private var isStopped: Boolean = false

    abstract fun chooseScenarioActionReceiver(): ScenarioActionReceiver?

    abstract fun addScenario(scenario: ScenarioActionReceiver)

    abstract fun removeScenario(scenario: ScenarioActionReceiver)

    abstract fun clear()

    @Synchronized
    override fun observeActions(scenario: Scenario): Observable<ScenarioAction> {
        return if (isStopped) {
            Observable.empty()
        } else {
            Observable.create { emitter ->
                val scenarioActionReceiver = ScenarioActionReceiver(scenario, emitter)
                addScenario(scenarioActionReceiver)
                if (!isSubscribed) {
                    subscribeOnDistributedActions()
                }
                emitter.setDisposable(Disposables.fromAction {
                    removeScenario(scenarioActionReceiver)
                })
            }
        }
    }

    @Synchronized
    private fun subscribeOnDistributedActions() {
        if (!isSubscribed) {
            isSubscribed = true
            observeScenarioActionArrivals
                .map { arrivalIntervalId ->
                    val scenarioTarget = chooseScenarioActionReceiver()
                    val action = scenarioTarget?.scenario?.createAction(
                        actionDefinition.name,
                        actionDefinition.actionParameters,
                        arrivalIntervalId
                    ) ?: NoOpAction
                    ScenarioActionExecutor(action, scenarioTarget?.emitter ?: EmptyEmitter)
                }
                .filter { it.scenarioAction !is NoOpAction }
                .doFinally {
                    stop()
                }
                .subscribe({
                    try {
                        it.emitter.onNext(it.scenarioAction)
                    } catch (e: Exception) {
                        logger.error("Error while notifying scenario to run an action", e)
                    }
                },
                    { e -> logger.error("Error in distributed actions notifications", e) })
        }
    }

    @Synchronized
    private fun stop() {
        isStopped = true
        clear()
    }
}
