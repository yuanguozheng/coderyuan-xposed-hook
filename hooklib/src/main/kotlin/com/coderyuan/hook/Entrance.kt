package com.coderyuan.hook

import android.content.Context
import com.coderyuan.hook.annotations.XPLoadPackageParam
import com.coderyuan.hook.model.HookItemModel
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.Exception
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * Hook模块入口类
 */
class Entrance : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val xposedHooksMap = getHookMap() ?: return
        val pkgName = lpparam?.packageName ?: return
        if (!xposedHooksMap.containsKey(pkgName)) {
            return
        }
        val hookItem = xposedHooksMap[pkgName] ?: return
        val implCls = hookItem.hookImplCls
        val impl = implCls.createInstance()

        val onHookApplicationCls = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                val classLoader: ClassLoader = if (hookItem.isUseMultiDex) {
                    (param.args[0] as? Context)?.classLoader
                } else {
                    lpparam.classLoader
                } ?: return
                implCls.members.forEach { m ->
                    m.findAnnotation<XPLoadPackageParam>() ?: return@forEach
                    if (m is KMutableProperty && m.returnType.classifier == XC_LoadPackage.LoadPackageParam::class) {
                        m.setter.call(impl, lpparam)
                    }
                }
                implCls.functions.forEach { f ->
                    handleHookMethod(f, impl, classLoader)
                }
            }
        }

        if (hookItem.isUseMultiDex) {
            XposedHelpers.findAndHookMethod(
                hookItem.applicationClassName,
                lpparam.classLoader,
                "attachBaseContext",
                Context::class.java,
                onHookApplicationCls
            )
        } else {
            XposedHelpers.findAndHookMethod(
                hookItem.applicationClassName,
                lpparam.classLoader,
                "onCreate",
                onHookApplicationCls
            )
        }
    }

    private fun getHookMap(): Map<String, HookItemModel>? {
        return try {
            val cls = Class.forName("com.coderyuan.hook.HooksManagerKt")
            val f = cls.getDeclaredField("xposedHooksMap")
            f.isAccessible = true
            f.get(null) as? Map<String, HookItemModel>
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

