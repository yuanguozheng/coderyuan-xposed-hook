package com.coderyuan.hook.compiler

import com.coderyuan.hook.annotations.XPHook
import com.coderyuan.hook.model.HookItemModel
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

private const val OUTPUT_OPTION_NAME = "kapt.kotlin.generated"

/**
 * 处理XPHook的注解，生成HooksManager
 */
@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(XPHook::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[OUTPUT_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return false
        }
        val fileName = "HooksManager"
        val fileBuilder = FileSpec.builder("com.coderyuan.hook", fileName)

        val list = mutableMapOf<String, Triple<String, Boolean, ClassName>>()

        val base = CodeBlock.builder().add("mapOf(\n")
        val targetCls = roundEnv.getElementsAnnotatedWith(XPHook::class.java)

        targetCls.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Only classes can be annotated"
                )
                return false
            }

            val clsName = (it as TypeElement).asClassName()
            val pkgName = it.getAnnotation(XPHook::class.java).pkgName
            val applicationName = it.getAnnotation(XPHook::class.java).applicationName
            val isUseMultiDex = it.getAnnotation(XPHook::class.java).isUseMultiDex
            list[pkgName] = Triple(applicationName, isUseMultiDex, clsName)
        }

        if (list.isEmpty()) {
            return false
        }

        list.keys.forEachIndexed { i, key ->
            base.add(
                CodeBlock.of(
                    "%S to %T(%S, %L, %T::class)",
                    key,
                    HookItemModel::class,
                    list[key]?.first,
                    list[key]?.second,
                    list[key]?.third
                )
            )
            if (i != (list.keys.size - 1)) {
                base.add(", \n")
            }
        }

        base.add("\n)").build()
        fileBuilder.addProperty(
            PropertySpec.builder(
                "xposedHooksMap", Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    HookItemModel::class.asClassName()
                )
            ).initializer(
                base.build()
            ).build()
        )
        fileBuilder.build().writeTo(File(kaptKotlinGeneratedDir))
        return true
    }
}