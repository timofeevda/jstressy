package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.scheduler.ScenarioRateScheduler.observeScenarioArrivals
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ScenarioRateSchedulerTest {

    companion object {

        private val testScheduler = TestScheduler()

        private val oneShotStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "One Shot Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "1s"
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

        private val constantRateStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Constant Rate Stage"
            override val scenarioName: String = ""
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

        private val constantRateRandomizedStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Constant Rate Randomized Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = true
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


        private val constantRatePoissonStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Constant Rate Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "100min"
            override val arrivalRate: Double = 0.005555 // one time per 3 minutes
            override val rampArrival: Double? = null
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String? = null
            override val rampDuration: String? = null
            override val scenariosLimit: Int? = null
            override val poissonArrival: Boolean = true
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }
        private val rampingRateStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Ramping Rate Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "10min"
            override val arrivalRate: Double = 1.0
            override val rampArrival: Double = 2.0 // target arrival rate
            override val rampArrivalRate: Double = 0.2 // increase arrival rate each 5 seconds
            override val rampArrivalPeriod: String = "5s"
            override val rampDuration: String = "5min" // increase arrival rate to target value in 5 minutes
            override val scenariosLimit: Int? = null
            override val poissonArrival: Boolean = false
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }

        // dedicated for testing rampArrivalPeriod
        private val rampingRateStageWithPeriod = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Ramping Rate Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "10min"
            override val arrivalRate: Double = 1.0
            override val rampArrival: Double = 2.0 // target arrival rate
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String = "5s" // increase arrival rate each 5 seconds
            override val rampDuration: String = "5min" // increase arrival rate to target value in 5 minutes
            override val scenariosLimit: Int? = null
            override val poissonArrival: Boolean = false
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }

        private val scenariosLimitStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Scenarios Limit Stage"
            override val scenarioName: String = ""
            override val delay: String = "1min"
            override val randomizeArrival: Boolean = false
            override val duration: String = "1min"
            override val arrivalRate: Double = 1.0
            override val rampArrival: Double? = null
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String? = null
            override val rampDuration: String? = null
            override val scenariosLimit: Int = 30
            override val poissonArrival: Boolean = false
            override val poissonMinRandom: Double? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        }

        private val arrivalIntervalsStage = object : StressyStage {
            override val actions: List<ScenarioActionDefinition> = emptyList()
            override val scenarioParameters: Map<String, String> = emptyMap()
            override val scenarioProviderParameters: Map<String, String> = emptyMap()
            override val name: String = "Scenarios Arrival Intervals Stage"
            override val scenarioName: String = ""
            override val delay: String? = null
            override val randomizeArrival: Boolean = false
            override val duration: String? = null
            override val arrivalRate: Double? = null
            override val rampArrival: Double? = null
            override val rampArrivalRate: Double? = null
            override val rampArrivalPeriod: String? = null
            override val rampDuration: String? = null
            override val poissonArrival: Boolean? = null
            override val poissonMinRandom: Double? = null
            override val scenariosLimit: Int? = null
            override val arrivalIntervals: MutableList<StressyArrivalInterval>
                get() = mutableListOf(
                        object : StressyArrivalInterval {
                            override val id: String = "first"
                            override val delay: String = "1min"
                            override val randomizeArrival: Boolean = false
                            override val duration: String = "10min"
                            override val arrivalRate: Double = 1.0
                            override val rampArrival: Double? = null
                            override val rampArrivalRate: Double? = null
                            override val rampArrivalPeriod: String? = null
                            override val rampDuration: String? = null
                            override val poissonArrival: Boolean = false
                            override val poissonMinRandom: Double? = null
                            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
                        },
                        object : StressyArrivalInterval {
                            override val id: String = "second"
                            override val delay: String = "12min"
                            override val randomizeArrival: Boolean = false
                            override val duration: String = "10min"
                            override val arrivalRate: Double = 1.0
                            override val rampArrival: Double = 2.0
                            override val rampArrivalRate: Double = 0.2
                            override val rampArrivalPeriod: String = "5s"
                            override val rampDuration: String = "5min"
                            override val poissonArrival: Boolean = false
                            override val poissonMinRandom: Double? = null
                            override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
                        }
                )
        }
    }

    @Test
    fun testConstantRateSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(constantRateStage)
                .subscribeOn(testScheduler)
                .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check if we've got tick value right after stage delay interval
        observer.assertValueCount(1)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(600)

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun testConstantRateRandomizedSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(constantRateRandomizedStage)
            .subscribeOn(testScheduler)
            .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(31, TimeUnit.SECONDS)

        // check if we've got tick value right after stage delay interval
        observer.assertValueCount(1)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(600)

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun testRampingRateSchedule() {
        testRampingStage(rampingRateStage)
    }

    @Test
    fun testRampingPeriodSchedule() {
        testRampingStage(rampingRateStageWithPeriod)
    }

    private fun testRampingStage(stage: StressyStage) {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(stage)
                .subscribeOn(testScheduler)
                .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        testScheduler.advanceTimeBy(5, TimeUnit.MINUTES)

        // just take current number of events emitted during ramp interval
        val eventsProcessed = observer.events[0].size

        // advance time and check that the target rate is correct one
        testScheduler.advanceTimeBy(5, TimeUnit.MINUTES)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(eventsProcessed + 596)

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun testScenariosLimitSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(scenariosLimitStage)
                .subscribeOn(testScheduler)
                .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check if we've got tick value right after stage delay interval
        observer.assertValueCount(1)

        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(30)

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun testArrivalIntervalsScenario() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(arrivalIntervalsStage)
                .subscribeOn(testScheduler)
                .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during the first interval delay
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check if we've got tick value right after stage delay interval
        observer.assertValueCount(1)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(600)

        // wait till next arrival interval starts to work
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        testScheduler.advanceTimeBy(5, TimeUnit.MINUTES)

        // just take current number of events emitted during ramp interval
        val eventsProcessed = observer.events[0].size

        // advance time and check that the target rate is correct one
        testScheduler.advanceTimeBy(5, TimeUnit.MINUTES)

        // check that we've got right number of ticks in the end of stage interval
        observer.assertValueCount(eventsProcessed + 596)

        observer.assertNoErrors()
        observer.assertComplete()
    }
    @Test
    fun testConstantRatePoissonSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(constantRatePoissonStage)
                .subscribeOn(testScheduler)
                .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        testScheduler.advanceTimeBy(100, TimeUnit.MINUTES)

        val eventsProcessed = observer.events[0].size

        assertTrue(eventsProcessed > 0)

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun testOneShotSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioArrivals(oneShotStage)
            .subscribeOn(testScheduler)
            .test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        // check that we haven't got any values during stage delay interval
        observer.assertSubscribed()
        observer.assertNotComplete()
        observer.assertValueCount(0)

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertValueCount(1)

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        observer.assertValueCount(1)
        observer.assertNoErrors()
        observer.assertComplete()
    }

}