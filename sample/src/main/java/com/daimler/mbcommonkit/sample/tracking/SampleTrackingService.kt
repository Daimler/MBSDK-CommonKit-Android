package com.daimler.mbcommonkit.sample.tracking

import com.daimler.mbcommonkit.tracking.TrackingService

interface SampleTrackingService : TrackingService {

    fun track(event: SampleTrackingEvent.MyEvent)
    fun track(event: SampleTrackingEvent.MyOtherEvent)
    fun track(event: SampleTrackingEvent.TheEvent)
    fun track(event: SampleTrackingEvent.MyComplexEvent)
}