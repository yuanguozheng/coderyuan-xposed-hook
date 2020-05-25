package com.coderyuan.hook.annotations

/**
 * 用来标记一个类，对应Hook一个App
 */
@Target(
    AnnotationTarget.CLASS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class XPHook(
    val pkgName: String,
    val applicationName: String,
    val isUseMultiDex: Boolean
)