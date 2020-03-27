package com.daimler.mbcommonkit.tracking

object MBTrackingService {

    private val mutableServices = mutableListOf<TrackingService>()
    val services: List<TrackingService>
        get() = mutableServices

    var trackingEnabled: Boolean = true

    fun <T : TrackingService> registerService(service: T): Boolean {
        if (!containsService(service)) {
            mutableServices.add(service)
            return true
        }
        return false
    }

    fun unregisterService(service: TrackingService): Boolean =
            mutableServices.remove(service)

    fun getService(id: String): TrackingService? = services.firstOrNull { it.id == id }

    inline fun <reified T : TrackingService> services(): List<T> =
            services.filterIsInstance(T::class.java)

    inline fun <reified T : TrackingService> trackEvent(event: TrackingEvent<T>) {
        if (trackingEnabled) services.forEach { if (it is T) event.track(it) }
    }

    private fun <T : TrackingService> containsService(service: T): Boolean =
            services.none { it.id == service.id }.not()
}