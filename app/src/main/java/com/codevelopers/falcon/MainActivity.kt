package com.codevelopers.falcon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.codevelopers.falcon.core.Falcon
import com.codevelopers.falcon.core.OnLocationListener
import com.codevelopers.falcon.poko.Spot
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var falcon: Falcon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        
        falcon = Falcon(this)
        falcon.listener = object : OnLocationListener {

            override fun firstTime(latLon: Spot) {}

            override fun onChangeLocation(latLon: Spot) {

                val text = "Spot latitude ${latLon.latitude} longitude ${latLon.longitude}"
                mainText.text = text
            }

            override fun notHasPermission() {
                Toast.makeText(this@MainActivity, "No tiene permiso", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        falcon.onResume()
    }

    override fun onPause() {
        super.onPause()
        falcon.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        falcon.onRequestPermissionsResult(requestCode, grantResults)
    }
}
