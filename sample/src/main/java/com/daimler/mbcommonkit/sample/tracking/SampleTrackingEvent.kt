package com.daimler.mbcommonkit.sample.tracking

import com.daimler.mbcommonkit.tracking.TrackingEvent

sealed class SampleTrackingEvent : TrackingEvent<SampleTrackingService> {

    class MyEvent(val timestamp: Long) : SampleTrackingEvent() {
        override fun track(trackingService: SampleTrackingService) {
            trackingService.track(this)
        }
    }

    class MyOtherEvent(val id: String) : SampleTrackingEvent() {
        override fun track(trackingService: SampleTrackingService) {
            trackingService.track(this)
        }
    }

    class TheEvent : SampleTrackingEvent() {
        override fun track(trackingService: SampleTrackingService) {
            trackingService.track(this)
        }
    }

    class MyComplexEvent(val model: SampleTrackingModel) : SampleTrackingEvent() {
        override fun track(trackingService: SampleTrackingService) {
            trackingService.track(this)
        }
    }
}