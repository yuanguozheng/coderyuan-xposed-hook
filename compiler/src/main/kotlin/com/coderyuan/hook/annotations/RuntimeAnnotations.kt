package com.coderyuan.hook.annotations

/**
 * 用来注入Xposed Params
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class XPLoadPackageParam