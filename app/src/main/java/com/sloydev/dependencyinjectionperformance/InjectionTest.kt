package com.sloydev.dependencyinjectionperformance

import com.sloydev.dependencyinjectionperformance.custom.DIContainer
import com.sloydev.dependencyinjectionperformance.custom.customJavaModule
import com.sloydev.dependencyinjectionperformance.custom.customKotlinModule
import com.sloydev.dependencyinjectionperformance.dagger2.DaggerJavaDaggerComponent
import com.sloydev.dependencyinjectionperformance.dagger2.DaggerKotlinDaggerComponent
import com.sloydev.dependencyinjectionperformance.katana.katanaJavaModule
import com.sloydev.dependencyinjectionperformance.katana.katanaKotlinModule
import com.sloydev.dependencyinjectionperformance.koin.koinJavaModule
import com.sloydev.dependencyinjectionperformance.koin.koinKotlinModule
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.rewedigital.katana.createComponent
import javax.inject.Inject

class InjectionTest : KoinComponent {

    private val kotlinDaggerTest = KotlinDaggerTest()
    private val javaDaggerTest = JavaDaggerTest()

    private val rounds = 100

    fun runTests(): List<LibraryResult> {
        val benchmarks = listOf(
            koinTest(),
            kodeinTest(),
            katanaTest(),
            customTest(),
            daggerTest()
        )
        // reportMarkdown(results)

        repeat(rounds) {
            benchmarks.forEach {
                it.kotlinBenchmark.run()
                it.javaBenchmark.run()
            }
        }

        return benchmarks.map {
            LibraryResult(
                injectorName = it.injectorName,
                kotlinResult = TestResult(
                    startupTime = it.kotlinBenchmark.setupTimes,
                    injectionTime = it.kotlinBenchmark.injectionTimes,
                    teardownTime = it.kotlinBenchmark.teardownTimes
                ),
                javaResult = TestResult(
                    startupTime = it.javaBenchmark.setupTimes,
                    injectionTime = it.javaBenchmark.injectionTimes,
                    teardownTime = it.javaBenchmark.teardownTimes
                )
            )
        }
    }

    private fun koinTest() = LibraryBenchmark(
        injectorName = "Koin",
        kotlinBenchmark = VariantBenchmark(
            { startKoin { modules(koinKotlinModule) } },
            { get<Fib8>() },
            { stopKoin() }
        ),
        javaBenchmark = VariantBenchmark(
            { startKoin { modules(koinJavaModule) } },
            { get<FibonacciJava.Fib8>() },
            { stopKoin() }
        )
    )

    private fun kodeinTest() = LibraryBenchmark(
        injectorName = "Kodein",
        kotlinBenchmark = VariantBenchmark(
            { Kodein { import(kodeinKotlinModule) } },
            { it.direct.instance<Fib8>() },
            {}
        ),
        javaBenchmark = VariantBenchmark(
            { Kodein { import(kodeinJavaModule) } },
            { it.direct.instance<FibonacciJava.Fib8>() },
            {}
        )
    )

    private fun katanaTest() = LibraryBenchmark(
        injectorName = "Katana",
        kotlinBenchmark = VariantBenchmark(
            { createComponent(modules = listOf(katanaKotlinModule)) },
            { it.injectNow<Fib8>() },
            {}
        ),
        javaBenchmark = VariantBenchmark(
            { createComponent(modules = listOf(katanaJavaModule)) },
            { it.injectNow<FibonacciJava.Fib8>() },
            {}
        )
    )

    private fun customTest() = LibraryBenchmark(
        injectorName = "Custom",
        kotlinBenchmark = VariantBenchmark(
            { DIContainer.loadModule(customKotlinModule) },
            { DIContainer.get<Fib8>() },
            { DIContainer.unloadModules() }
        ),
        javaBenchmark = VariantBenchmark(
            { DIContainer.loadModule(customJavaModule) },
            { DIContainer.get<FibonacciJava.Fib8>() },
            { DIContainer.unloadModules() }
        )
    )

    private fun daggerTest() = LibraryBenchmark(
        injectorName = "Dagger",
        kotlinBenchmark = VariantBenchmark(
            { DaggerKotlinDaggerComponent.create() },
            { it.inject(kotlinDaggerTest) },
            {}
        ),
        javaBenchmark = VariantBenchmark(
            { DaggerJavaDaggerComponent.create() },
            { it.inject(javaDaggerTest) },
            {}
        )
    )

    class KotlinDaggerTest {
        @Inject
        lateinit var daggerFib8: Fib8
    }

    class JavaDaggerTest {
        @Inject
        lateinit var daggerFib8: FibonacciJava.Fib8
    }
}
