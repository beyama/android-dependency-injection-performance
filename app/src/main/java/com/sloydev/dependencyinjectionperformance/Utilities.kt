package com.sloydev.dependencyinjectionperformance

import android.content.res.Resources
import android.util.Log
import java.util.*
import kotlin.system.measureNanoTime

typealias Nanoseconds = Long
typealias Milliseconds = Double

class LibraryBenchmark(
    val injectorName: String,
    val kotlinBenchmark: VariantBenchmark<*>,
    val javaBenchmark: VariantBenchmark<*>
)

class VariantBenchmark<T>(
    val setup: () -> T,
    val test: (T) -> Unit,
    val teardown: (T) -> Unit
) {

    var setupTimes = mutableListOf<Long>()
    var injectionTimes = mutableListOf<Long>()
    var teardownTimes = mutableListOf<Long>()

    fun run() {
        var instance: T? = null
        setupTimes.add(measureNanoTime { instance = setup() })
        injectionTimes.add(measureNanoTime { test(instance!!) })
        teardownTimes.add(measureNanoTime { teardown(instance!!) })
    }

}

data class LibraryResult(
    val injectorName: String,
    val kotlinResult: TestResult,
    val javaResult: TestResult
)

data class TestResult(
    val startupTime: List<Nanoseconds>,
    val injectionTime: List<Nanoseconds>,
    val teardownTime: List<Nanoseconds>
)

fun Nanoseconds.toMilliseconds(): Milliseconds = this / 1000000.0

fun Milliseconds?.format() = String.format(Locale.ENGLISH, "%.2f ms", this)

fun List<Nanoseconds>.median() = sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }

fun log(msg: String) {
    Log.i("DI-TEST", msg)
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()