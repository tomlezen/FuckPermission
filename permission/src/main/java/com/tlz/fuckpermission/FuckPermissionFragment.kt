package com.tlz.fuckpermission

import androidx.fragment.app.Fragment

/**
 * By tomlezen
 * Create at 2018/11/30
 */
internal class FuckPermissionFragment : Fragment() {

    private var permissionResult: FuckPermissionResult? = null
    private var reqTag: String? = null

    internal fun requestPermissions(permissionResult: FuckPermissionResult, reqTag: String, permissions: Array<String>) {
        this@FuckPermissionFragment.permissionResult = permissionResult
        this.reqTag = reqTag
        requestPermissions(permissions, REQUEST_CODE)
    }

    fun removeSelf() {
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNowAllowingStateLoss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE && activity != null) {
            permissionResult?.onRequestPermissionsResult(activity!!, reqTag, requestCode, permissions, grantResults)
        } else {
            removeSelf()
        }
    }

    companion object {
        private const val REQUEST_CODE = 10101
    }

}