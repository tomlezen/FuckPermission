package com.tlz.fuckpermission

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.tlz.fuckpermission.adapters.ActivityLifecycleCallbacksAdapter
import com.tlz.fuckpermission.annotations.FuckPermission
import com.tlz.fuckpermission.annotations.FuckPermissionSurpportFragment

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

    companion object : FuckPermissionProcessor {

        private var sInstance: FuckPermissionProcessor? = null

        operator fun invoke(): FuckPermissionProcessor = sInstance ?: FuckPermissionProcessorImpl().also { sInstance = null }

        /**
         * 安装权限处理器..
         * @param app: Application
         */
        override fun install(app: Application) {
            FuckPermissionProcessor().install(app)
        }

        /**
         * 卸载权限处理器.
         * @param app Application
         */
        override fun uninstall(app: Application) {
            FuckPermissionProcessor().uninstall(app)
        }

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

    /**
     * 请求权限.
     */
    @Deprecated(replaceWith = ReplaceWith("request"), message = "")
    fun requestPermission()

    /**
     * 请求权限.
     */
    fun request()

    companion object {
        internal operator fun invoke(request: FuckPermissionRequest, tag: Any): FuckPermissionOperate =
            FuckPermissionOperateImpl(request, tag)
    }
}

private class FuckPermissionOperateImpl(private val request: FuckPermissionRequest, private val tag: Any) :
    FuckPermissionOperate {
    override fun requestPermission() {
        request()
    }

    override fun request() {
        request.checkAndRequestPermission(tag)
    }
}

internal interface FuckPermissionRequest {
    fun checkAndRequestPermission(tag: Any)
}


internal interface FuckPermissionResult {
    fun onRequestPermissionsResult(
        act: Activity,
        tag: Any?,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
}

class FuckPermissionRequestCallbackWrapper internal constructor() {

    private var onBefore: (() -> Unit)? = null
    private var onAfter: (() -> Unit)? = null
    private var onGranted: ((FuckPermissionOperate) -> Unit)? = null
    private var onRevoked: ((FuckPermissionOperate, Array<String>, Array<String>, Boolean) -> Unit)? = null

    internal var callback: FuckPermissionCallback = object : FuckPermissionCallback {

        override fun onFuckPermissionBefore() {
            onBefore?.invoke()
        }

        override fun onFuckPermissionGranted(operate: FuckPermissionOperate) {
            onGranted?.invoke(operate)
        }

        override fun onFuckPermissionRevoked(
            operate: FuckPermissionOperate,
            grantedPermissions: Array<String>,
            revokedPermissions: Array<String>,
            canShowRequestPermissionRationale: Boolean
        ) {
            onRevoked?.invoke(operate, grantedPermissions, revokedPermissions, canShowRequestPermissionRationale)
        }

    }

    /**
     * 权限请求回调.
     * 调用该方法后：onFuckPermissionBefore等方法将失效.
     * @param callback FuckPermissionCallback
     */
    fun requestCallback(callback: FuckPermissionCallback) {
        this.callback = callback
    }

    /**
     * 权限请求之前。
     * @param onBefore () -> Unit
     * @return FuckPermissionRequestCallbackWrapper
     */
    fun onFuckPermissionBefore(onBefore: () -> Unit): FuckPermissionRequestCallbackWrapper {
        this.onBefore = onBefore
        return this
    }

    /**
     * 权限请求之后.
     * @param onAfter () -> Unit
     * @return FuckPermissionRequestCallbackWrapper
     */
    fun onFuckPermissionAfter(onAfter: () -> Unit): FuckPermissionRequestCallbackWrapper {
        this.onAfter = onAfter
        return this
    }

    /**
     * 权限被授予.
     * @param onGranted (operate: FuckPermissionOperate) -> Unit
     * @return FuckPermissionRequestCallbackWrapper
     */
    fun onFuckPermissionGranted(onGranted: (operate: FuckPermissionOperate) -> Unit): FuckPermissionRequestCallbackWrapper {
        this.onGranted = onGranted
        return this
    }

    /**
     * 权限被拒绝.
     * @param onRevoked (
    operate: FuckPermissionOperate,
    grantedPermissions: Array<String>,
    revokedPermissions: Array<String>,
    canShowRequestPermissionRationale: Boolean
    ) -> Unit
     * @return FuckPermissionRequestCallbackWrapper
     */
    fun onFuckPermissionRevoked(
        onRevoked: (
            operate: FuckPermissionOperate,
            grantedPermissions: Array<String>,
            revokedPermissions: Array<String>,
            canShowRequestPermissionRationale: Boolean
        ) -> Unit
    ): FuckPermissionRequestCallbackWrapper {
        this.onRevoked = onRevoked
        return this
    }
}

private class FuckPermissionProcessorImpl : FuckPermissionProcessor, FuckPermissionRequest, FuckPermissionResult {

    /** 是否已安装. */
    private var isInstalled = false

    private val permissionItems = mutableMapOf<Any, FuckPermissionItem>()

    /** Activity生命周期回调. */
    private val activityLifecycleCallbacks by lazy {
        object : ActivityLifecycleCallbacksAdapter() {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                if (activity is FragmentActivity) {
                    activity.javaClass.getAnnotation(FuckPermissionSurpportFragment::class.java)?.let {

                    }
                }
            }

            override fun onActivityResumed(activity: Activity?) {
                activity?.let {
                    if (!permissionItems.contains(it)) {
                        this@FuckPermissionProcessorImpl.requestPermissionFromAnnotation(it)
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity?) {
                activity?.let { permissionItems.remove(activity) }
            }
        }
    }

    /**
     * 通过注解方式的权限请求.
     * @param act Activity
     */
    private fun requestPermissionFromAnnotation(act: Activity) {
        val fuckPermission = act.javaClass.getAnnotation(FuckPermission::class.java)
        if (act is FragmentActivity && fuckPermission?.permissions?.isNotEmpty() == true) {
            val permissionCallback = act as? FuckPermissionCallback
            val permissionItem = permissionItems g act
            if (permissionItem.isRequesting) return
            requestPermissions(act.supportFragmentManager, act, fuckPermission.permissions, permissionCallback)
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

    override fun checkAndRequestPermission(tag: Any) {
        permissionItems[tag]?.let {
//            val fm = when (tag) {
//                is FragmentActivity -> tag.supportFragmentManager
//                is Fragment -> tag.childFragmentManager
//            }
            requestPermissions()
        }
    }

    /**
     * 请求权限.
     * @param fm FragmentManager
     * @param tag Any
     * @param permissions Array<String>
     * @param callback FuckPermissionCallback?
     */
    private fun requestPermissions(
        fm: FragmentManager,
        tag: Any,
        permissions: Array<String>,
        callback: FuckPermissionCallback?
    ) {
        val permissionItem = permissionItems g tag
        if (permissionItem.isRequesting) return
        permissionItem.callback = callback
        fm.registerFragmentLifecycleCallbacks()
        callback?.onFuckPermissionBefore()
        val frg = permissionItem.frg
        if (!frg.isAdded) {
            fm.beginTransaction().add(frg, FuckPermissionFragment::class.java.canonicalName)
                .commitNowAllowingStateLoss()
        }
        permissionItem.isRequesting = true
        frg.requestPermissions(this, tag, permissions)
    }

    override fun onRequestPermissionsResult(
        act: Activity,
        tag: Any?,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        tag?.let { _tag ->
            val permissionItem = permissionItems g _tag
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
                val operate = FuckPermissionOperate(this, _tag)
                if (revokeds.isEmpty()) {
                    callback.onFuckPermissionGranted(operate)
                } else {
                    callback.onFuckPermissionRevoked(
                        operate,
                        granteds.toTypedArray(),
                        revokeds.toTypedArray(),
                        FuckPermissionProcessor.shouldShowRequestPermissionRationale(act, *(revokeds.toTypedArray()))
                    )
                }
                callback.onFuckPermissionAfter()
            }
        }
    }

    fun request(act: Activity, permissions: Array<String>): FuckPermissionRequestCallbackWrapper {
        if (act is FragmentActivity) {
            val callbackWrapper = FuckPermissionRequestCallbackWrapper()
            requestPermissions(act.supportFragmentManager, act, permissions, callbackWrapper.callback)
            return callbackWrapper
        }
        throw IllegalArgumentException("Activity must be FragmentActivity")
    }

    fun request(frg: Fragment, permissions: Array<String>): FuckPermissionRequestCallbackWrapper {
        return frg.activity?.let { request(it, permissions) }
            ?: throw IllegalStateException("Fragment " + this + " not attached to Activity")
    }


    infix fun MutableMap<Any, FuckPermissionItem>.g(tag: Any): FuckPermissionItem =
        this[tag] ?: FuckPermissionItem().also {
            this[tag] = it
        }

    data class FuckPermissionItem(var isRequesting: Boolean = false, var callback: FuckPermissionCallback? = null, var frg: FuckPermissionFragment = FuckPermissionFragment())

}