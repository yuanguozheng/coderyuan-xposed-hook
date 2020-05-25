package com.coderyuan.hook

import com.coderyuan.hook.annotations.HookMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * 打印Log
 *
 * @param tag Tag
 * @param content 日志内容
 */
fun log(tag: String?, content: String?) {
    tag ?: return
    content ?: return
    XposedBridge.log("$tag: $content")
}

/**
 * 打印堆栈信息
 *
 * @param tag Tag，默认StackTrace
 */
fun printStackTrace(tag: String = "StackTrace") {
    log(tag, "\n" + Exception().stackTrace.joinToString("\n"))
}

/**
 * 根据名称获取属性值
 *
 * @param obj 实例
 * @param propName 属性名
 */
fun getPropValueByName(obj: Any?, propName: String?): Any? {
    obj ?: return null
    propName ?: return null
    val prop = obj::class.memberProperties.find { it.name == "$propName" }
    prop?.isAccessible = true
    return prop?.getter?.call(obj)
}

/**
 * 调用方法
 *
 * @param f 反射获取的方法
 * @param impl 实例
 * @param param Xposed Params
 */
fun invokeFunction(
    f: KFunction<*>,
    impl: Any,
    param: XC_MethodHook.MethodHookParam?
) {
    f.isAccessible = true
    // 如果instanceParameter为空，则证明是static方法，第一个参数不能传this
    if (f.instanceParameter == null) {
        if (f.parameters.isEmpty()) {
            f.call()
        } else {
            f.call(param)
        }
    } else {
        if (f.parameters.isEmpty()) {
            f.call(impl)
        } else {
            f.call(impl, param)
        }
    }
}

/**
 * 处理方法Hook
 *
 * @param f 反射获取的方法
 * @param impl 实例
 * @param classLoader ClassLoader
 */
fun handleHookMethod(
    f: KFunction<*>,
    impl: Any,
    classLoader: ClassLoader
) {
    val hookMethodAnnotation = f.findAnnotation<HookMethod>() ?: return
    var paramsClsArray: Array<Class<*>>? = null
    val paramsClsStrArray: Array<String>? = hookMethodAnnotation.paramsClsStr
    if (hookMethodAnnotation.paramsCls.isNotEmpty()) {
        val arr = mutableListOf<Class<*>>()
        hookMethodAnnotation.paramsCls.forEach { item ->
            arr.add(item.java)
        }
        paramsClsArray = arr.toTypedArray()
    }
    val onHookMethod = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            super.afterHookedMethod(param)
            if (!hookMethodAnnotation.isHookBefore) {
                invokeFunction(f, impl, param)
            }
        }

        override fun beforeHookedMethod(param: MethodHookParam?) {
            super.beforeHookedMethod(param)
            if (hookMethodAnnotation.isHookBefore) {
                invokeFunction(f, impl, param)
            }
        }
    }

    if (paramsClsArray == null) {
        if (paramsClsStrArray?.isNotEmpty() == true) {
            if (hookMethodAnnotation.isConstructor) {
                XposedHelpers.findAndHookConstructor(
                    hookMethodAnnotation.className,
                    classLoader,
                    hookMethodAnnotation.methodName,
                    *paramsClsStrArray,
                    onHookMethod
                )
            } else {
                XposedHelpers.findAndHookMethod(
                    hookMethodAnnotation.className,
                    classLoader,
                    hookMethodAnnotation.methodName,
                    *paramsClsStrArray,
                    onHookMethod
                )
            }
        } else {
            if (hookMethodAnnotation.isConstructor) {
                XposedHelpers.findAndHookConstructor(
                    hookMethodAnnotation.className,
                    classLoader,
                    hookMethodAnnotation.methodName,
                    onHookMethod
                )
            } else {
                XposedHelpers.findAndHookMethod(
                    hookMethodAnnotation.className,
                    classLoader,
                    hookMethodAnnotation.methodName,
                    onHookMethod
                )
            }
        }
    } else {
        if (hookMethodAnnotation.isConstructor) {
            XposedHelpers.findAndHookConstructor(
                hookMethodAnnotation.className,
                classLoader,
                hookMethodAnnotation.methodName,
                *paramsClsArray,
                onHookMethod
            )
        } else {
            XposedHelpers.findAndHookMethod(
                hookMethodAnnotation.className,
                classLoader,
                hookMethodAnnotation.methodName,
                *paramsClsArray,
                onHookMethod
            )
        }
    }
}