package com.coderyuan.hook.annotations

import kotlin.reflect.KClass

/**
 * 用来标记Hook一个方法的函数
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class HookMethod(
    val className: String = "",
    val methodName: String = "",
    val isHookBefore: Boolean = false,
    val paramsCls: Array<KClass<*>> = [],
    val paramsClsStr: Array<String> = [],
    val isConstructor: Boolean = false
)