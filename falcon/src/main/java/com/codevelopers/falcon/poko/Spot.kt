package com.codevelopers.falcon.poko

import android.location.Location

class Spot() {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(latitude: Double, longitude: Double) : this() {

        this.latitude = latitude
        this.longitude = longitude
    }

    fun location(location: Location) {

        this.latitude = location.latitude
        this.longitude = location.longitude
    }
}