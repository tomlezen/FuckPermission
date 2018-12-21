package com.tlz.fuckpermission

/**
 * 权限处理回调.
 * By tomlezen
 * Create at 2018/11/20
 */
interface FuckPermissionCallback {

    /**
     * 权限请求之前回调.
     */
    fun onFuckPermissionBefore() {}

    /**
     * 权限请求之后.
     */
    fun onFuckPermissionAfter() {}

    /**
     * 权限被授予.
     * @param operate FuckPermissionOperate
     */
    fun onFuckPermissionGranted(operate: FuckPermissionOperate)

    /**
     * 权限请求被拒绝.
     * @param operate FuckPermissionAction
     * @param grantedPermissions Array<String> 被授予的权限
     * @param revokedPermissions Array<String> 被拒绝的权限
     * @param canShowRequestPermissionRationale Boolean 是否还可以显示权限申请框.
     */
    fun onFuckPermissionRevoked(operate: FuckPermissionOperate, grantedPermissions: Array<String>, revokedPermissions: Array<String>, canShowRequestPermissionRationale: Boolean)

}