package com.daimler.mbcommonkit.sample.tracking

import com.daimler.mbcommonkit.tracking.TrackingEvent

sealed class OtherSampleTrackingEvent : TrackingEvent<OtherSampleTrackingService> {

    class SampleEvent : OtherSampleTrackingEvent() {
        override fun track(trackingService: OtherSampleTrackingService) {
            trackingService.track(this)
        }
    }
}