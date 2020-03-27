package com.daimler.mbcommonkit.sample.tracking

import com.daimler.mbcommonkit.tracking.TrackingService

interface OtherSampleTrackingService : TrackingService {

    fun track(event: OtherSampleTrackingEvent.SampleEvent)
}