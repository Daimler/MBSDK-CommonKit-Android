package com.daimler.mbcommonkit.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.daimler.mbcommonkit.preferences.PreferenceObserver
import com.daimler.mbcommonkit.sample.tracking.OtherSampleTrackingEvent
import com.daimler.mbcommonkit.sample.tracking.SampleTrackingEvent
import com.daimler.mbcommonkit.sample.tracking.SampleTrackingModel
import com.daimler.mbcommonkit.tracking.MBTrackingService
import com.daimler.mbloggerkit.MBLoggerKit
import com.daimler.mbloggerkit.export.shareAsFile
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {

    private val prefs: CommonPreferences
        get() = CommonApplication.getPreferences(this)

    private val trackingObserver = object : PreferenceObserver<Boolean> {
        override fun onChanged(newValue: Boolean) {
            MBTrackingService.trackingEnabled = newValue
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackingEnabled = prefs.trackingEnabled.get()
        MBTrackingService.trackingEnabled = trackingEnabled

        startedText.text = prefs.startCounter.get().toString()
        trackSwitch.isChecked = trackingEnabled
        setListeners()

        dummyLogs()

        prefs.trackingEnabled.observe(trackingObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.trackingEnabled.stopObserving(trackingObserver)
    }

    private fun setListeners() {
        taskButton.setOnClickListener { start(TaskActivity::class) }
        trackButton.setOnClickListener { trackEvents() }
        prefsTimeButton.setOnClickListener { logMeasurementTimeFromPrefs() }

        trackSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.trackingEnabled.set(isChecked)
        }
    }

    private fun logMeasurementTimeFromPrefs() {
        (application as CommonApplication).prefs.logReadAndWriteTimes()
    }

    private fun dummyLogs() = repeat(10) { MBLoggerKit.d("Log entry $it.") }

    private fun start(cls: KClass<*>) {
        startActivity(Intent(this, cls.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.share_log -> shareCurrentLog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareCurrentLog() {
        val currentLog = MBLoggerKit.loadCurrentLog()
        currentLog.shareAsFile(this, "log_output")
    }

    private fun trackEvents() {
        MBTrackingService.trackEvent(SampleTrackingEvent.MyEvent(System.currentTimeMillis()))
        MBTrackingService.trackEvent(SampleTrackingEvent.MyOtherEvent(UUID.randomUUID().toString()))
        MBTrackingService.trackEvent(SampleTrackingEvent.TheEvent())
        MBTrackingService.trackEvent(SampleTrackingEvent.MyComplexEvent(
                SampleTrackingModel(
                        UUID.randomUUID().toString(),
                        System.currentTimeMillis(),
                        Random.nextInt(),
                        Random.nextBoolean()
                )
        ))
        MBTrackingService.trackEvent(OtherSampleTrackingEvent.SampleEvent())
    }
}
