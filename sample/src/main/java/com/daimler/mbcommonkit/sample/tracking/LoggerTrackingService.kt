package com.daimler.mbcommonkit.sample.tracking

import com.daimler.mbloggerkit.MBLoggerKit

class LoggerTrackingService : SampleTrackingService, OtherSampleTrackingService {

    override val id: String = this::class.java.canonicalName

    override fun track(event: SampleTrackingEvent.MyEvent) {
        MBLoggerKit.d("MyEvent -> ${event.timestamp}", TAG)
    }

    override fun track(event: SampleTrackingEvent.MyOtherEvent) {
        MBLoggerKit.d("MyOtherEvent -> ${event.id}", TAG)
    }

    override fun track(event: SampleTrackingEvent.TheEvent) {
        MBLoggerKit.d("TheEvent", TAG)
    }

    override fun track(event: SampleTrackingEvent.MyComplexEvent) {
        MBLoggerKit.d("MyComplexEvent -> ${event.model}", TAG)
    }

    override fun track(event: OtherSampleTrackingEvent.SampleEvent) {
        MBLoggerKit.d("SampleEvent", TAG)
    }

    private companion object {
        private val TAG = LoggerTrackingService::class.java.simpleName
    }
}