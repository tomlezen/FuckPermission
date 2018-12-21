package com.tlz.fuckpermission.annotations

/**
 * 权限标记注解.
 *
 * By tomlezen
 * Create at 2018/11/20
 * @property permissions 需要申请的权限.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class FuckPermission(val permissions: Array<String>)