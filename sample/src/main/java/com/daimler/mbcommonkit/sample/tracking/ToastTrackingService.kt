package com.daimler.mbcommonkit.sample.tracking

import android.content.Context
import android.widget.Toast

class ToastTrackingService(private val context: Context) : OtherSampleTrackingService {

    override val id: String = this::class.java.canonicalName

    override fun track(event: OtherSampleTrackingEvent.SampleEvent) {
        Toast.makeText(context, "SampleEvent", Toast.LENGTH_SHORT).show()
    }
}