package com.tlz.fuckpermission

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import com.tlz.fuckpermission.annotations.FuckPermission

/**
 * 权限处理器.
 * By tomlezen
 * Create at 2018/11/20
 */
interface FuckPermissionProcessor {

    /**
     * 安装权限处理器..
     * @param app: Application
     */
    fun install(app: Application)

    /**
     * 卸载权限处理器.
     * @param app Application
     */
    fun uninstall(app: Application)

    companion object {
        operator fun invoke(): FuckPermissionProcessor = FuckPermissionProcessorImpl()

        /**
         * 是否可以还可以显示权限请求框.
         * @param act Activity
         * @param permissions Array<out String>
         * @return Boolean
         */
        fun shouldShowRequestPermissionRationale(act: Activity, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return false
            }
            return shouldShowRequestPermissionRationaleM(act, *permissions)
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun shouldShowRequestPermissionRationaleM(act: Activity, vararg permissions: String): Boolean {
            return permissions.any { !isGranted(act, it) && act.shouldShowRequestPermissionRationale(it) }
        }

        /**
         * 权限是否被允许.
         * @param act Activity
         * @param permission String
         * @return Boolean
         */
        fun isGranted(act: Activity, permission: String): Boolean =
            ActivityCompat.checkSelfPermission(act, permission) == PackageManager.PERMISSION_GRANTED

        /**
         * 权限是被否拒绝.
         * @param act Activity
         * @param permission String
         * @return Boolean
         */
        fun isRevoked(act: Activity, permission: String): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isRevokedM(act, permission)

        @TargetApi(Build.VERSION_CODES.M)
        private fun isRevokedM(act: Activity, permission: String): Boolean {
            return act.packageManager.isPermissionRevokedByPolicy(permission, act.packageName)
        }
    }

}

interface FuckPermissionOperate {

    fun requestPermission()

    companion object {
        internal operator fun invoke(request: FuckPermissionRequest, act: Activity): FuckPermissionOperate = FuckPermissionOperateImpl(request, act)
    }
}

private class FuckPermissionOperateImpl(private val request: FuckPermissionRequest, private val act: Activity) : FuckPermissionOperate {
    override fun requestPermission() {
        request.checkAndRequestPermission(act)
    }
}

internal interface FuckPermissionRequest {
    fun checkAndRequestPermission(act: Activity)
}


internal interface FuckPermissionResult {
    fun onRequestPermissionsResult(act: Activity?, requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}

private open class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {

    }

    override fun onActivityResumed(activity: Activity?) {

    }

    override fun onActivityStarted(activity: Activity?) {

    }

    override fun onActivityDestroyed(activity: Activity?) {

    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

    }

    override fun onActivityStopped(activity: Activity?) {

    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

    }

}

private class FuckPermissionProcessorImpl : FuckPermissionProcessor, FuckPermissionRequest, FuckPermissionResult {

    /** 是否已安装. */
    private var isInstalled = false

    private val permissionItems = mutableMapOf<Activity, FuckPermissionItem>()

    /** Activity生命周期回调. */
    private val activityLifecycleCallbacks by lazy {
        object : ActivityLifecycleCallbacksAdapter() {
            override fun onActivityResumed(activity: Activity?) {
                activity?.let {
                    if (!permissionItems.contains(it)) {
                        this@FuckPermissionProcessorImpl.checkAndRequestPermission(it)
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity?) {
                activity?.let { permissionItems.remove(activity) }
            }
        }
    }

    override fun install(app: Application) {
        if (isInstalled) return
        app.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun uninstall(app: Application) {
        if (!isInstalled) return
        app.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun checkAndRequestPermission(act: Activity) {
        val fuckPermission = act.javaClass.getAnnotation(FuckPermission::class.java)
        if (act is FragmentActivity && fuckPermission?.permissions?.isNotEmpty() == true) {
            val permissionCallback = act as? FuckPermissionCallback
            val permissionItem = permissionItems gg act
            if (permissionItem.isRequesting) return
            permissionItem.callback = permissionCallback
            permissionCallback?.onFuckPermissionBefore()
            val frg = permissionItem.frg
            if (!frg.isAdded) {
                act.supportFragmentManager?.beginTransaction()?.add(frg, FuckPermissionFragment::class.java.canonicalName)?.commitNowAllowingStateLoss()
            }
            permissionItem.isRequesting = true
            frg.requestPermissions(this, fuckPermission.permissions)
        }
    }

    override fun onRequestPermissionsResult(act: Activity?, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        act?.let { _act ->
            val permissionItem = permissionItems gg act
            permissionItem.callback?.let { callback ->
                val granteds = mutableListOf<String>()
                val revokeds = mutableListOf<String>()
                permissions.forEachIndexed { index, s ->
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        granteds.add(s)
                    } else {
                        revokeds.add(s)
                    }
                }
                permissionItem.frg.removeSelf()
                permissionItem.isRequesting = false
                val operate = FuckPermissionOperate(this, _act)
                if (revokeds.isEmpty()) {
                    callback.onFuckPermissionGranted(operate)
                } else {
                    callback.onFuckPermissionRevoked(operate, granteds.toTypedArray(), revokeds.toTypedArray(), FuckPermissionProcessor.shouldShowRequestPermissionRationale(_act, *(revokeds.toTypedArray())))
                }
                callback.onFuckPermissionAfter()
            }
        }
    }

    infix fun MutableMap<Activity, FuckPermissionItem>.gg(act: Activity): FuckPermissionItem =
        this[act] ?: FuckPermissionItem().also {
            this[act] = it
        }

    data class FuckPermissionItem(var isRequesting: Boolean = false, var callback: FuckPermissionCallback? = null, var frg: FuckPermissionFragment = FuckPermissionFragment())

}