@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.proton.core.test.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * An interface meant to be implemented by a Test Suite that uses Complex Concurrency via Coroutines.
 * Example:
```
class MyClassTest : CoroutinesTest by coroutinesTest {
// test cases
}
```
 *
 * It provides a [CoroutinesTestRule] and alternative dispatchers.
 *
 * @author Davide Farella
 */
interface CoroutinesTest {
    @get:Rule val coroutinesRule: CoroutinesTestRule

    val mainDispatcher: ExecutorCoroutineDispatcher
    val ioDispatcher: ExecutorCoroutineDispatcher
    val compDispatcher: ExecutorCoroutineDispatcher
}

/** @see CoroutinesTest */
val coroutinesTest = object : CoroutinesTest {
    override val mainDispatcher = newSingleThreadContext("UI thread")
    override val ioDispatcher = newSingleThreadContext("IO thread pool")
    override val compDispatcher = newSingleThreadContext("Computational thread pool")

    override val coroutinesRule = CoroutinesTestRule(
        mainDispatcher, ioDispatcher, compDispatcher
    )
}

/**
 * A JUnit Test Rule that set a Main Dispatcher
 * @author Davide Farella
 */
class CoroutinesTestRule internal constructor(
    private val mainDispatcher: ExecutorCoroutineDispatcher = newSingleThreadContext("UI thread"),
    private val ioDispatcher: ExecutorCoroutineDispatcher = newSingleThreadContext("IO thread pool"),
    private val compDispatcher: ExecutorCoroutineDispatcher = newSingleThreadContext("Computational thread pool")
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(mainDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainDispatcher.close()
        ioDispatcher.close()
        compDispatcher.close()
    }
}
