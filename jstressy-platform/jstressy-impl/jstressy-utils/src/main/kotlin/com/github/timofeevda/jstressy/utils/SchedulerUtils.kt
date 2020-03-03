package com.github.timofeevda.jstressy.utils

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object SchedulerUtils {
    /**
     * Get observable holding the next Poisson arrival event
     *
     * @param arrivalRate arrival rate
     */
    fun observeNextPoissonArrival(arrivalRate: Double) =
            Observable.timer(nextPoissonArrival(arrivalRate), TimeUnit.MILLISECONDS)

    /**
     * Get observable holding the next Poisson arrival event
     *
     * @param arrivalRate arrival rate
     * @param maxRandom max random value which can be used to achieve bigger intervals between Poisson arrivals
     */
    fun observeNextPoissonArrival(arrivalRate: Double, maxRandom: Double) =
            Observable.timer(nextPoissonArrival(arrivalRate, maxRandom), TimeUnit.MILLISECONDS)

    /**
     * Next Poisson arrivals is calculated based on the following formula:
     * -1 / λ * ln(r), where λ - arrival rate, r - uniformly distributed random number [0, 1)
     *
     * @param arrivalRate arrival rate
     *
     * @return interval in milliseconds when the next Poisson arrival should happen
     */
    fun nextPoissonArrival(arrivalRate: Double) = ((-1 / arrivalRate) * Math.log(Math.random()) * 1000).toLong()

    /**
     * Next Poisson arrivals is calculated based on the following formula:
     * -1 / λ * ln(r), where λ - arrival rate, r - uniformly distributed random number between 0 and
     * specified max random
     *
     * This particular method is useful for getting bigger interval between Poisson arrivals
     *
     * @param arrivalRate arrival rate
     * @param maxRandom max random value
     *
     * @return interval in milliseconds when the next Poisson arrival should happen
     */
    fun nextPoissonArrival(arrivalRate: Double, maxRandom: Double) = ((-1 / arrivalRate) * Math.log(maxRandom * Math.random()) * 1000).toLong()
}