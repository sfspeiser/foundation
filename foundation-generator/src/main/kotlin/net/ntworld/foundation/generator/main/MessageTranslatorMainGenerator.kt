package net.ntworld.foundation.generator.main

import com.squareup.kotlinpoet.*
import net.ntworld.foundation.generator.Framework
import net.ntworld.foundation.generator.GeneratedFile
import net.ntworld.foundation.generator.GeneratorOutput
import net.ntworld.foundation.generator.Utility
import net.ntworld.foundation.generator.type.ClassInfo
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.ntworld.foundation.generator.setting.ContractSetting
import net.ntworld.foundation.generator.setting.MessagingSetting
import net.ntworld.foundation.generator.type.Property

object MessageTranslatorMainGenerator {
    fun generate(
        setting: ContractSetting,
        messaging: MessagingSetting?,
        implementation: ClassInfo,
        properties: Map<String, Property>
    ): GeneratedFile {
        val target = Utility.findMessageTranslatorTarget(setting)
        val file = buildFile(setting, messaging, implementation, properties, target)
        val stringBuffer = StringBuffer()
        file.writeTo(stringBuffer)

        return GeneratedFile.makeMainFile(target, stringBuffer.toString())
    }

    private fun buildFile(
        setting: ContractSetting,
        messaging: MessagingSetting?,
        implementation: ClassInfo,
        properties: Map<String, Property>,
        target: ClassInfo
    ): FileSpec {
        val file = FileSpec.builder(target.packageName, target.className)
        GeneratorOutput.addHeader(file, this::class.qualifiedName)
        file.addType(buildClass(setting, messaging, implementation, properties, target))

        return file.build()
    }

    private fun buildClass(
        setting: ContractSetting,
        messaging: MessagingSetting?,
        implementation: ClassInfo,
        properties: Map<String, Property>,
        target: ClassInfo
    ): TypeSpec {
        val type = TypeSpec.objectBuilder(target.className)
            .addSuperinterface(
                Framework.MessageTranslator.parameterizedBy(
                    setting.contract.toClassName()
                )
            )
            .addProperty(
                PropertySpec.builder("json", Framework.Json)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(
                        "Json{ignoreUnknownKeys = true}",
                        Framework.JsonConfiguration
                    )
                    .build()
            )

        buildMakeFunctionIfNeeded(setting, implementation, properties, type)
        buildCanConvertFunction(setting, messaging, type)
        buildFromMessageFunction(setting, implementation, type)
        buildToMessageFunction(setting, messaging, implementation, type)
        return type.build()
    }

    private fun buildMakeFunctionIfNeeded(
        setting: ContractSetting,
        implementation: ClassInfo,
        properties: Map<String, Property>,
        type: TypeSpec.Builder
    ) {
        if (setting.contract == implementation) {
            return
        }

        val code = CodeBlock.builder()
        code.beginControlFlow("if (instance is %T)", implementation.toClassName())
        code.add("return instance\n")
        code.endControlFlow()
        code.add("return %T(\n", implementation.toClassName())
        code.indent()
        val fields = properties.values.filter { !it.hasBody }
        val lastIndex = fields.size - 1
        fields.forEachIndexed { index, field ->
            code.add("%L = instance.%L", field.name, field.name)
            if (index != lastIndex) {
                code.add(",")
            }
            code.add("\n")
        }
        code.unindent()
        code.add(")\n")

        type.addFunction(
            FunSpec.builder("make")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("instance", setting.contract.toClassName())
                .returns(implementation.toClassName())
                .addCode(code.build())
                .build()
        )
    }

    private fun buildCanConvertFunction(setting: ContractSetting, messaging: MessagingSetting?, type: TypeSpec.Builder) {
        val code = CodeBlock.builder()
        if (null !== messaging && messaging.type.isNotEmpty()) {
            code.add("val type = message.attributes[\"type\"]\n")
            code.add(
                "return null !== type && type.stringValue == %S\n",
                messaging.type
            )
        } else {
            code.add("val bodyType = message.attributes[\"bodyType\"]\n")
            code.add(
                "return null !== bodyType && bodyType.stringValue == %S\n",
                setting.contract.fullName()
            )
        }


        type.addFunction(
            FunSpec.builder("canConvert")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("message", Framework.Message)
                .returns(Boolean::class)
                .addCode(code.build())
                .build()
        )
    }

    private fun buildFromMessageFunction(setting: ContractSetting, implementation: ClassInfo, type: TypeSpec.Builder) {
        type.addFunction(
            FunSpec.builder("fromMessage")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("message", Framework.Message)
                .returns(setting.contract.toClassName())
                .addStatement("return json.decodeFromString(%T.serializer(), message.body)", implementation.toClassName())
                .build()
        )
    }

    private fun buildToMessageFunction(setting: ContractSetting, messaging: MessagingSetting?, implementation: ClassInfo, type: TypeSpec.Builder) {
        val code = CodeBlock.builder()
        code.add("val attributes = mapOf<String, %T>(\n", Framework.MessageAttribute)
        code.indent()
        if (null !== messaging && messaging.type.isNotEmpty()) {
            code
                .add(
                    "\"type\" to %T.createStringAttribute(\n",
                    Framework.MessageUtility
                )
                .indent()
                .add("%S\n", messaging.type)
                .unindent()
                .add("),\n")
        }

        code
            .add(
                "\"bodyType\" to %T.createStringAttribute(\n",
                Framework.MessageUtility
            )
            .indent()
            .add("%S\n", setting.contract.fullName())
            .unindent()
            .add("),\n")

        code.add(
            "\"implementationType\" to %T.createStringAttribute(\n",
            Framework.MessageUtility
        )
            .indent()
            .add("%S\n", implementation.fullName())
            .unindent()
            .add(")\n")

        code.unindent()
        code.add(")\n")
        code.add("\n")

        code.add("return MessageUtility.createMessage(\n")
        code.indent()

        if (setting.contract == implementation) {
            code.add("json.encodeToString(%T.serializer(), input),\n", implementation.toClassName())
        } else {
            code.add("json.encodeToString(%T.serializer(), make(input)),\n", implementation.toClassName())
        }
        code.add("attributes\n")

        code.unindent()
        code.add(")\n")

        type.addFunction(
            FunSpec.builder("toMessage")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("input", setting.contract.toClassName())
                .returns(Framework.Message)
                .addCode(code.build())
                .build()
        )
    }
}