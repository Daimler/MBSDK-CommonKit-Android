package com.daimler.mbcommonkit.sample

import android.content.SharedPreferences
import com.daimler.mbcommonkit.extensions.*
import com.daimler.mbcommonkit.security.RandomStringGenerator
import com.daimler.mbcommonkit.utils.logExecutionTime
import com.daimler.mbloggerkit.MBLoggerKit
import java.util.*

class CommonPreferences(private val preferences: SharedPreferences) {

    val startCounter = preferences.intPreference(KEY_START_COUNTER)
    val trackingEnabled = preferences.booleanPreference(KEY_TRACKING_ENABLED)
    private val testSet = preferences.stringSetPreference(KEY_TEST_SET)
    private val testBoolean = preferences.booleanPreference(KEY_TEST_BOOLEAN)
    private val testLong = preferences.longPreference(KEY_TEST_LONG)
    private val testFloat = preferences.floatPreference(KEY_TEST_FLOAT)
    private val testString = preferences.stringPreference(KEY_TEST_STRING)
    private val initialized = preferences.booleanPreference(KEY_INITIALIZED)

    init {
        if (!initialized.get()) initialize()
    }

    fun incrementStartCounter() {
        startCounter.set(startCounter.get().inc())
        val set = HashSet(testSet.get())
        set.add(startCounter.get().toString())
        testSet.set(set)
    }

    fun logReadAndWriteTimes() {
        MBLoggerKit.d("--- Read and Write times ---")
        repeat(10) {
            logWriteTimes()
            logReadTimes()
        }
    }

    private fun logWriteTimes() {
        logExecutionTime("Write to prefs") {
            preferences.edit().putString("abcd", "defg").apply()
        }
    }

    private fun logReadTimes() {
        logExecutionTime("Read from prefs") {
            preferences.getString("abcd", "")
        }
    }

    fun all() = preferences.all

    private fun initialize() {
        val random = Random()
        testBoolean.set(true)
        testLong.set(random.nextLong())
        testFloat.set(random.nextFloat())
        testString.set(RandomStringGenerator().generateString(random.nextInt(100) + 1))
        initialized.set(true)
    }

    companion object {

        private const val KEY_START_COUNTER = "start.counter"
        private const val KEY_TEST_SET = "set.test"
        private const val KEY_TEST_BOOLEAN = "boolean.test"
        private const val KEY_TEST_LONG = "long.test"
        private const val KEY_TEST_FLOAT = "float.test"
        private const val KEY_TEST_STRING = "string.test"
        private const val KEY_INITIALIZED = "prefs.initialized"
        private const val KEY_TRACKING_ENABLED = "prefs.tracking.enabled"
    }
}