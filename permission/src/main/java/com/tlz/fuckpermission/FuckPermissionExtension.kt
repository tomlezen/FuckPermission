@file:Suppress("NOTHING_TO_INLINE")

package com.tlz.fuckpermission

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * By tomlezen
 * Create at 2019/1/17
 */

/**
 * 安装权限处理器.
 * @receiver Application
 */
inline fun Application.installFuckPermission() = FuckPermissionProcessor.install(this)

inline fun Activity.installFuckPermission() = application.installFuckPermission()

/**
 * 请求权限.
 * @receiver FragmentActivity
 * @param permissions Array<String>
 * @return FuckPermissionRequestCallbackWrapper
 */
inline fun FragmentActivity.requestPermission(permissions: Array<String>): FuckPermissionRequestCallbackWrapper =
    FuckPermissionProcessor.request(this, permissions)

/**
 * 请求权限.
 * @receiver Fragment
 * @param permissions Array<String>
 * @return FuckPermissionRequestCallbackWrapper
 */
inline fun Fragment.requestPermission(permissions: Array<String>): FuckPermissionRequestCallbackWrapper =
    activity?.requestPermission(permissions) ?: throw IllegalStateException("Fragment " + this + " not attached to Activity")

