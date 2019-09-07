package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.scheduler.ScenarioRateScheduler.observeScenarioTicks
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ScenarioRateSchedulerTest {

    companion object {

        private val testScheduler = TestScheduler()

        private val constantRateStage = object : StressyStage {
            override val scenarioParameters: Map<String, String>
                get() = emptyMap()
            override val scenarioProviderParameters: Map<String, String>
                get() = emptyMap()
            override val name: String
                get() = "Constant Rate Stage"
            override val scenarioName: String
                get() = ""
            override val stageDelay: String?
                get() = "1min"
            override val stageDuration: String
                get() = "10min"
            override val arrivalRate: Double
                get() = 1.0
            override val rampArrival: Double?
                get() = null
            override val rampArrivalRate: Double?
                get() = null
            override val rampInterval: String?
                get() = null
            override val scenariosLimit: Int?
                get() = null
        }

        private val rampingRateStage = object : StressyStage {
            override val scenarioParameters: Map<String, String>
                get() = emptyMap()
            override val scenarioProviderParameters: Map<String, String>
                get() = emptyMap()
            override val name: String
                get() = "Ramping Rate Stage"
            override val scenarioName: String
                get() = ""
            override val stageDelay: String?
                get() = "1min"
            override val stageDuration: String
                get() = "10min"
            override val arrivalRate: Double
                get() = 1.0
            override val rampArrival: Double?
                get() = 2.0 // target arrival rate
            override val rampArrivalRate: Double?
                get() = 0.2 // increase arrival rate each 5 seconds
            override val rampInterval: String?
                get() = "5min" // increase arrival rate to target value in 5 minutes
            override val scenariosLimit: Int?
                get() = null
        }

        private val scenariosLimitStage = object : StressyStage {
            override val scenarioParameters: Map<String, String>
                get() = emptyMap()
            override val scenarioProviderParameters: Map<String, String>
                get() = emptyMap()
            override val name: String
                get() = "Scenarios Limit Stage"
            override val scenarioName: String
                get() = ""
            override val stageDelay: String?
                get() = "1min"
            override val stageDuration: String
                get() = "1min"
            override val arrivalRate: Double
                get() = 1.0
            override val rampArrival: Double?
                get() = null
            override val rampArrivalRate: Double?
                get() = null
            override val rampInterval: String?
                get() = null
            override val scenariosLimit: Int?
                get() = 30
        }
    }

    @Test
    fun testConstantRateSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioTicks(constantRateStage)
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
    fun testRampingRateSchedule() {
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

        val observer = observeScenarioTicks(rampingRateStage)
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

        val observer = observeScenarioTicks(scenariosLimitStage)
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
}