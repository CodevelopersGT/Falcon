package com.codevelopers.falcon.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class Context {

    private var fragment: Fragment? = null
    private var activity: FragmentActivity? = null

    constructor(fragment: Fragment) {

        this.fragment = fragment
    }

    constructor(activity: FragmentActivity) {

        this.activity = activity
    }

    fun isActivity(): Boolean {

        return activity != null
    }


    fun context(): Context? {

        if (fragment != null)
            return fragment?.context
        else if (activity != null)
            return activity?.applicationContext


        return null
    }

    fun hasPermission(vararg permissions: String): Boolean =
        hasPermissions(permissions.toList().toTypedArray())


    private fun hasPermissions(permissions: Array<String>): Boolean {

        for (permission in permissions) {
            if (context()?.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }

        return true
    }

    fun requestPermissions(
        requestCode: Int,
        vararg permissions: String
    ) {


        if (isActivity())
            activity?.requestPermissions(permissions.toList().toTypedArray(), requestCode)
        else if (fragment != null)
            fragment?.requestPermissions(permissions.toList().toTypedArray(), requestCode)
    }

    fun shouldShowRequestPermissionRationale(permission: String): Boolean {

        if (permission.isEmpty())
            throw RuntimeException("Permission is empty")


        if (isActivity())
            return activity?.shouldShowRequestPermissionRationale(permission) ?: false
        else if (fragment != null)
            return fragment?.shouldShowRequestPermissionRationale(permission) ?: false

        return false
    }

    fun hasAllPermissions(
        mRequestCode: Int,
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {


        if (mRequestCode != requestCode)
            return false

        for (permission in grantResults) {

            if (permission != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }
}