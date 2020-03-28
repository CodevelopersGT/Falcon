package com.codevelopers.falcon.core

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.codevelopers.falcon.poko.Spot
import com.codevelopers.falcon.util.Context
import kotlin.math.atan
import kotlin.math.sin
import kotlin.math.tan

private const val REQUEST_PERMISSION = 23

class Falcon : LocationListener {

    var spot: Spot = Spot(0.0, 0.0)
    private var context: Context
    private var manager: LocationManager? = null
    private var firstTime = true
    private var count: Int = 0

    var listener: OnLocationListener? = null

    var minimalDistance = 2F
        set(value) {
            field = if (value < 1) minimalDistance else value
        }
    var requestTime = 3000L
        set(value) {

            field = if (value < 1000) requestTime else value

        }


    constructor(activity: FragmentActivity) {

        this.context = Context(activity)
    }

    constructor(fragment: Fragment) {

        this.context = Context(fragment)
    }

    fun onResume() {

        //ask if i has permissions to request gps
        if (manager != null)
            return

        if (!context.hasPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {

            requestLocationPermissions()
            return
        }

        //Active location
        activeLocation()
    }

    private fun requestLocationPermissions() {

        context.requestPermissions(
            REQUEST_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun onPause() {

        stopLocationRequest()
    }

    @SuppressLint("MissingPermission")
    private fun activeLocation() {

        //Logic to request location
        manager = context.context()
            ?.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager


        try {

            manager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                requestTime,
                minimalDistance,
                this
            )

            manager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                requestTime,
                minimalDistance,
                this
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun stopLocationRequest() {

        //Logic to stop location

        manager?.removeUpdates(this)
        manager = null
    }

    override fun onLocationChanged(location: Location?) {


        location?.let {

            spot.location(location)

            if (firstTime) {

                firstTime = false
                listener?.firstTime(spot)
            }

            listener?.onChangeLocation(spot)

        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("Not yet implemented")
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("Not yet implemented")
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {

        if (requestCode != REQUEST_PERMISSION)
            return

        if (context.hasAllPermissions(REQUEST_PERMISSION, requestCode, grantResults)) {
            onResume()
        } else//The user don't has permission then retrieve code
        {
            if (count > 1)
                return

            count++
            listener?.notHasPermission()
        }
    }

    fun distance(
        initLatLon: Spot,
        finishLatLon: Spot
    ): Float {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        var lat1 = initLatLon.latitude
        var lon1 = initLatLon.longitude
        var lat2 = finishLatLon.latitude
        var lon2 = finishLatLon.longitude
        val MAXITERS = 20
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0
        lat2 *= Math.PI / 180.0
        lon1 *= Math.PI / 180.0
        lon2 *= Math.PI / 180.0
        val a = 6378137.0 // WGS84 major axis
        val b = 6356752.3142 // WGS84 semi-major axis
        val f = (a - b) / a
        val aSqMinusBSqOverBSq = (a * a - b * b) / (b * b)
        val L = lon2 - lon1
        var A = 0.0
        val U1 = atan((1.0 - f) * tan(lat1))
        val U2 = atan((1.0 - f) * tan(lat2))
        val cosU1 = kotlin.math.cos(U1)
        val cosU2 = kotlin.math.cos(U2)
        val sinU1 = sin(U1)
        val sinU2 = sin(U2)
        val cosU1cosU2 = cosU1 * cosU2
        val sinU1sinU2 = sinU1 * sinU2
        var sigma = 0.0
        var deltaSigma = 0.0
        var cosSqAlpha = 0.0
        var cos2SM = 0.0
        var cosSigma = 0.0
        var sinSigma = 0.0
        var cosLambda = 0.0
        var sinLambda = 0.0
        var lambda = L // initial guess
        for (iter in 0 until MAXITERS) {
            val lambdaOrig = lambda
            cosLambda = Math.cos(lambda)
            sinLambda = Math.sin(lambda)
            val t1 = cosU2 * sinLambda
            val t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            val sinSqSigma = t1 * t1 + t2 * t2 // (14)
            sinSigma = Math.sqrt(sinSqSigma)
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda // (15)
            sigma = Math.atan2(sinSigma, cosSigma) // (16)
            val sinAlpha =
                if (sinSigma == 0.0) 0.0 else cosU1cosU2 * sinLambda / sinSigma // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SM =
                if (cosSqAlpha == 0.0) 0.0 else cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha // (18)
            val uSquared = cosSqAlpha * aSqMinusBSqOverBSq // defn
            A = 1 + uSquared / 16384.0 *  // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)))
            val B = uSquared / 1024.0 *  // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)))
            val C = f / 16.0 *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)) // (10)
            val cos2SMSq = cos2SM * cos2SM
            deltaSigma = B * sinSigma *  // (6)
                    (cos2SM + B / 4.0 *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    B / 6.0 * cos2SM *
                                    (-3.0 + 4.0 * sinSigma * sinSigma) *
                                    (-3.0 + 4.0 * cos2SMSq)))
            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                    (sigma + C * sinSigma *
                            (cos2SM + C * cosSigma *
                                    (-1.0 + 2.0 * cos2SM * cos2SM))) // (11)
            val delta = (lambda - lambdaOrig) / lambda
            if (kotlin.math.abs(delta) < 1.0e-12) {
                break
            }
        }
        return (b * A * (sigma - deltaSigma)).toFloat()
    }

}

public interface OnLocationListener {

    //This method is launched  to first location
    fun firstTime(latLon: Spot)

    //This method is launched every time, location changed
    fun onChangeLocation(latLon: Spot)

    fun notHasPermission()
}