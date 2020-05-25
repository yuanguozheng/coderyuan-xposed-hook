package com.coderyuan.hook

import com.coderyuan.hook.annotations.HookMethod
import com.coderyuan.hook.annotations.XPHook
import com.coderyuan.hook.annotations.XPLoadPackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

@XPHook(
    // 需要Hook的App包名
    pkgName = "com.a.b",
    // 目标App的Application完整类名
    applicationName = "com.a.b.XXApplication",
    // 是否使用MultiDex模式，true则会在hook attachBaseContext方法以后再去hook其他方法，false则是在hook onCreate以后
    isUseMultiDex = true
)
class Example {

    // 全局LoadPackageParam，可选
    @XPLoadPackageParam
    var params: XC_LoadPackage.LoadPackageParam? = null

    @HookMethod(
        // 需要Hook的方法所在类的完整类名
        className = "com.a.b.C",
        // 方法名
        methodName = "setUrl",
        // 是否调用beforeHookedMethod，默认为false，设为true则会在afterHookedMethod中调用
        isHookBefore = false,
        // 参数类型数组，paramsCls和paramsClsStr任选一个，不能同时使用
        paramsCls = [String::class]
    )
    // param参数可选，如果用不到参数，可以不写
    fun methodTest(param: XC_MethodHook.MethodHookParam?) {
        // todo 实现hook以后的操作
        // 如果Hook的是非static类型的实例方法，可以用如下方法获取类变量的值
        param?.args?.elementAtOrNull(0)?.let {
            getPropValueByName(it, "mUrl")
        }
    }

    @HookMethod(
        // 需要Hook的方法所在类的完整类名
        className = "com.a.b",
        // 是否调用beforeHookedMethod，默认为false，设为true则会在afterHookedMethod中调用
        isHookBefore = false,
        // 是否Hook构造函数，默认false，设为true后，methodName无效
        isConstructor = true
    )
    // param参数可选，如果用不到参数，可以不写
    fun constructorTest(param: XC_MethodHook.MethodHookParam?) {
        // todo 实现hook以后的操作
        // 示例：打印调用堆栈
        printStackTrace("MethodHookStack")
    }
}