package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ActionsDistributionSchedulingTest {
    companion object {

        private val testScheduler = TestScheduler()

        private val constantRateStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = listOf(object : ScenarioActionDefinition {
                override val name: String = "action"
                override val actionParameters: Map<String, String> = emptyMap()
                override val distributionMode: ActionDistributionMode = ActionDistributionMode.ROUND_ROBIN
                override val run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)? =
                    null
                override val arrivalRate: Double = 3.0
                override val rampArrival: Double? = null
                override val rampArrivalRate: Double? = null
                override val rampArrivalPeriod: String? = null
                override val rampDuration: String? = null
                override val poissonArrival: Boolean? = null
                override val poissonMinRandom: Double? = null
                override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
                override val duration: String = "1min"
                override val delay: String? = null
                override val randomizeArrival: Boolean = false

            }, object : ScenarioActionDefinition {
                override val name: String = "action"
                override val actionParameters: Map<String, String> = emptyMap()
                override val distributionMode: ActionDistributionMode = ActionDistributionMode.ROUND_ROBIN
                override val run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)? =
                    null
                override val arrivalRate: Double = 3.0
                override val rampArrival: Double? = null
                override val rampArrivalRate: Double? = null
                override val rampArrivalPeriod: String? = null
                override val rampDuration: String? = null
                override val poissonArrival: Boolean? = null
                override val poissonMinRandom: Double? = null
                override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
                override val duration: String = "1min"
                override val delay: String? = null
                override val randomizeArrival: Boolean = false

            })
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Constant Rate Stage"
            override val scenarioName: String = "Test"
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "10min"
            override val arrivalRate: Double = 1.0
            override val rampArrival: Double? = null
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String? = null
            override val rampDuration: String? = null
            override val scenariosLimit: Int? = null
            override val poissonArrival: Boolean = false
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }

        private val roundRobinDistributionTestStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = listOf(object : ScenarioActionDefinition {
                override val name: String = "action"
                override val actionParameters: Map<String, String> = emptyMap()
                override val distributionMode: ActionDistributionMode = ActionDistributionMode.ROUND_ROBIN
                override val run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)? =
                    null
                override val arrivalRate: Double = 2.0
                override val rampArrival: Double? = null
                override val rampArrivalRate: Double? = null
                override val rampArrivalPeriod: String? = null
                override val rampDuration: String? = null
                override val poissonArrival: Boolean? = null
                override val poissonMinRandom: Double? = null
                override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
                override val duration: String = "3min"
                override val delay: String = "4min"
                override val randomizeArrival: Boolean = false

            })
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Constant Rate Stage"
            override val scenarioName: String = "Test"
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "3min"
            override val arrivalRate: Double = 1.0
            override val rampArrival: Double? = null
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String? = null
            override val rampDuration: String? = null
            override val scenariosLimit: Int? = null
            override val poissonArrival: Boolean = false
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }

    }

    @Test
    fun constantRateSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val stressyScenariosScheduler = scenariosScheduler(constantRateStage)

        val observer = stressyScenariosScheduler.observeScenariosWithActions()
            .flatMap { swa -> stressyScenariosScheduler.observeScenarioActions(swa) }
            .subscribeOn(testScheduler)
            .test()

        testScheduler.advanceTimeBy(60, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(2)

        testScheduler.advanceTimeBy(60, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertSubscribed()
        observer.assertNotComplete()
        // 180 actions from scenarios invoked within minute and additional action from the most recent scenario created
        // in the last second
        observer.assertValueCount(362)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        // no actions after 1 minute defined for actions
        observer.assertValueCount(362)

        observer.assertNoErrors()

    }

    @Test
    fun noEndlessLoopsIfNoActionsAvailable() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val stressyScenariosScheduler = scenariosScheduler(constantRateStage, false)

        val observer = stressyScenariosScheduler.observeScenariosWithActions()
            .flatMap { swa -> stressyScenariosScheduler.observeScenarioActions(swa) }
            .subscribeOn(testScheduler)
            .test()

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        observer.assertNoErrors()
        observer.assertSubscribed()
        observer.assertValueCount(0)
    }

    @Test
    fun roundRobinEvenlyDistributed() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val scenariosCounter = AtomicInteger(0)
        val actionsCountMap: HashMap<Int, AtomicInteger> = HashMap()

        val scenariosScheduler =
            scenariosScheduler(roundRobinDistributionTestStage, true, scenariosCounter, actionsCountMap)

        val observer = scenariosScheduler.observeScenariosWithActions()
            .flatMap { swa -> scenariosScheduler.observeScenarioActions(swa) }
            .subscribeOn(testScheduler)
            .doOnNext {
                it.run()
            }
            .test()

        testScheduler.advanceTimeBy(4, TimeUnit.MINUTES)

        observer.assertNoErrors()
        observer.assertSubscribed()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        observer.assertNoErrors()
        observer.assertSubscribed()
        observer.assertValueCount(360)

        actionsCountMap.forEach { (_, actionsCount) ->
            assert(actionsCount.get() == 2) { "Each scenario has 2 actions invocations according to round-robing strategy" }
        }

    }

    private fun scenariosScheduler(
        stage: StressyStage,
        availableForDistributedAction: Boolean = true,
        scenariosCounter: AtomicInteger = AtomicInteger(0),
        actionsCountMap: HashMap<Int, AtomicInteger> = HashMap()
    ): StressyScenariosScheduler {
        val mockVertxService = mockk<VertxService>()

        val mockRequestExecutorService = mockk<RequestExecutorService>()

        val mockConfigurationService = mockk<ConfigurationService>()

        val mockMetricsRegistryService = mockk<MetricsRegistryService>()

        every { mockMetricsRegistryService.get() } returns mockk()

        val scenarioProvider = mockk<ScenarioProvider>()
        every { scenarioProvider.get() } answers { _ ->
            TestScenario(
                availableForDistributedAction,
                scenariosCounter,
                actionsCountMap
            )
        }

        val scenarioProviderService = mockk<ScenarioProviderService>()
        every { scenarioProvider.init(any(), any(), any(), any(), any()) } returns Unit
        every { scenarioProviderService[any()] } returns scenarioProvider

        val scenarioRegistryService = mockk<ScenarioRegistryService>()
        every { scenarioRegistryService[any()] } returns scenarioProviderService

        every { mockConfigurationService.configuration.stressPlan.stages } returns listOf(stage)

        return StressyScenariosScheduler(
            mockVertxService,
            mockRequestExecutorService,
            mockConfigurationService,
            mockMetricsRegistryService,
            scenarioRegistryService
        )
    }

    class TestScenario(
        private val availableForDistributedAction: Boolean = true,
        scenariosCounter: AtomicInteger = AtomicInteger(0),
        private val actionsCountMap: HashMap<Int, AtomicInteger> = HashMap()
    ) : Scenario {

        private var distributionId: String? = null

        private var scenarioNumber = 0

        init {
            scenarioNumber = scenariosCounter.incrementAndGet()
        }

        override fun createAction(
            action: String,
            parameters: Map<String, String>,
            run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)?,
            intervalId: String
        ): ScenarioAction {
            return object : ScenarioAction {
                override fun run() {
                    actionsCountMap.computeIfAbsent(scenarioNumber) { _ -> AtomicInteger(0) }.incrementAndGet()
                }

            }
        }

        override fun withActionDistributionId(id: String): Scenario {
            distributionId = id
            return this
        }

        override fun getActionDistributionId(): String? = distributionId

        override fun isAvailableForActionDistribution(): Boolean = availableForDistributedAction

        override fun start(actions: List<ScenarioActionDefinition>) {

        }

        override fun stop() {

        }

        override fun withArrivalInterval(intervalId: String): Scenario {
            return this
        }

        override fun withParameters(parameters: Map<String, String>): Scenario {
            return this
        }

    }

}