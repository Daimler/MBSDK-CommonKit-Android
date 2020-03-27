package com.daimler.mbcommonkit.sample

import android.app.Application
import android.content.Context
import com.daimler.mbcommonkit.extensions.getEncryptedSharedPreferences
import com.daimler.mbcommonkit.preferences.PreferenceObserver
import com.daimler.mbcommonkit.sample.tracking.LoggerTrackingService
import com.daimler.mbcommonkit.sample.tracking.ToastTrackingService
import com.daimler.mbcommonkit.security.Crypto
import com.daimler.mbcommonkit.tracking.MBTrackingService
import com.daimler.mbloggerkit.MBLoggerKit
import com.daimler.mbloggerkit.PrinterConfig
import com.daimler.mbloggerkit.adapter.AndroidLogAdapter
import com.daimler.mbloggerkit.adapter.PersistingLogAdapter

class CommonApplication : Application() {

    lateinit var prefs: CommonPreferences

    private val observer1 = object : PreferenceObserver<Int> {
        override fun onChanged(newValue: Int) {
            MBLoggerKit.d("1 | Changed to $newValue.")
            prefs.startCounter.stopObserving(this)
        }
    }

    private val observer2 = object : PreferenceObserver<Int> {
        override fun onChanged(newValue: Int) {
            MBLoggerKit.d("2 | Changed to $newValue.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val loggingEnabled = BuildConfig.DEBUG
        val printerConfig = PrinterConfig.Builder()
            .addAdapter(AndroidLogAdapter.Builder()
                .setLoggingEnabled(loggingEnabled)
                .build())
            .addAdapter(PersistingLogAdapter.Builder(this)
                .setLoggingEnabled(loggingEnabled)
                .useMemoryLogging()
                .build())
        MBLoggerKit.usePrinterConfig(printerConfig.build())
        val crypto = Crypto(this, true)
        val encryptedPrefs = createNewPrefs(crypto)
        prefs = CommonPreferences(encryptedPrefs)
        prefs.startCounter.observe(observer1)
        prefs.startCounter.observe(observer2)
        prefs.incrementStartCounter()
        MBLoggerKit.d("${prefs.all()}")

        repeat(10) {
            CommonPreferences(createNewPrefs(crypto)).logReadAndWriteTimes()
        }

        MBTrackingService.apply {
            registerService(LoggerTrackingService())
            registerService(ToastTrackingService(this@CommonApplication))
        }
    }

    private fun createNewPrefs(crypto: Crypto) = getEncryptedSharedPreferences(
        "com.daimler.mm.common.alias", "settings.common", crypto = crypto
    )

    companion object {

        fun getPreferences(context: Context): CommonPreferences =
            (context.applicationContext as CommonApplication).prefs
    }
}