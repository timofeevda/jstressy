package com.github.timofeevda.jstressy.utils

import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import kotlin.math.ln

object SchedulerUtils {
    /**
     * Get observable holding the next Poisson arrival event with min random 0.0001 for not getting zero value for
     * natural logarithm
     *
     * @param arrivalRate arrival rate
     */
    fun observeNextPoissonArrival(arrivalRate: Double): Observable<Long> =
        Observable.timer(nextPoissonArrival(arrivalRate, 0.0001), TimeUnit.MILLISECONDS)

    /**
     * Get observable holding the next Poisson arrival event
     *
     * @param arrivalRate arrival rate
     * @param minRandom min random value which can be used to achieve smaller intervals between Poisson arrivals
     */
    fun observeNextPoissonArrival(arrivalRate: Double, minRandom: Double): Observable<Long> =
        Observable.timer(nextPoissonArrival(arrivalRate, minRandom), TimeUnit.MILLISECONDS)

    /**
     * Next Poisson arrivals is calculated based on the following formula:
     * -1 / λ * ln(r), where λ - arrival rate, r - uniformly distributed random number [0, 1)
     *
     * @param arrivalRate arrival rate
     *
     * @return interval in milliseconds when the next Poisson arrival should happen
     */
    private fun nextPoissonArrival(arrivalRate: Double) = ((-1 / arrivalRate) * ln(Math.random()) * 1000).toLong()

    /**
     * Next Poisson arrivals is calculated based on the following formula:
     * -1 / λ * ln(r), where λ - arrival rate, r - uniformly distributed random number between 0 and
     * specified max random
     *
     * This particular method is useful for getting smaller interval between Poisson arrivals
     *
     * @param arrivalRate arrival rate
     * @param minRandom min random value
     *
     * @return interval in milliseconds when the next Poisson arrival should happen
     */
    private fun nextPoissonArrival(arrivalRate: Double, minRandom: Double) = ((-1 / arrivalRate) * ln(minRandom + ((1 - minRandom)  * Math.random())) * 1000).toLong()
}