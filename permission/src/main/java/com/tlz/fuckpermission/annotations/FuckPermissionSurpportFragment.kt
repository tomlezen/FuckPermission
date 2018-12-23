package com.tlz.fuckpermission.annotations

/**
 * 在Activity标记支持Fragment.
 * 但是不支持Fragment中的Fragment.
 * Created by Tomlezen.
 * Date: 2018/12/23.
 * Time: 10:12 PM.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class FuckPermissionSurpportFragment