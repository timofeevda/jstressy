package com.github.timofeevda.jstressy.utils

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object SchedulerUtils {
    /**
     * Get observable holding the next Poisson arrival event
     *
     * @param arrivalRate arrival rate
     */
    fun observeNextPoissonArrival(arrivalRate: Double?) =
            Observable.timer(nextPoissonArrival(arrivalRate ?: 1.0), TimeUnit.MILLISECONDS)

    /**
     * Next Poisson arrivals is calculated based on the following formula:
     * -1 / λ * ln(r), where λ - arrival rate, r - uniformly distributed random number [0, 1)
     *
     * @param arrivalRate arrival rate
     *
     * @return interval in milliseconds when the next Poisson arrival should happen
     */
    fun nextPoissonArrival(arrivalRate: Double) = ((-1 / arrivalRate) * Math.log(Math.random()) * 1000).toLong()
}