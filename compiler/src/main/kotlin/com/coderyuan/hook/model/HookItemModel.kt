package com.coderyuan.hook.model

import kotlin.reflect.KClass

/**
 * 用来描述一个Hook
 */
data class HookItemModel(
    val applicationClassName: String,
    val isUseMultiDex: Boolean,
    val hookImplCls: KClass<*>
)