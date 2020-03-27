package com.daimler.mbcommonkit.tracking

interface TrackingEvent<T : TrackingService> {

    fun track(trackingService: T)
}