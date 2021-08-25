package net.ntworld.foundation.eventSourcing

import kotlinx.serialization.json.Json
import net.ntworld.foundation.exception.DecryptException
import net.ntworld.foundation.Infrastructure

object EventEntityConverterUtility {
    private const val ENCRYPTED_CIPHER_ID_KEY = "__encrypted@cipherId"
    private const val ENCRYPTED_ALGORITHM_KEY = "__encrypted@algorithm"
    private const val ENCRYPTED_DATA_KEY = "__encrypted@data"

    data class ProcessResult(
        val data: String,
        val metadata: String
    )

    data class Setting(
        val metadata: Boolean = false,
        val encrypted: Boolean = false,
        val faked: String = ""
    )

    fun processRawJson(
        infrastructure: Infrastructure,
        json: Json,
        fields: Map<String, Setting>,
        raw: String
    ): ProcessResult {
        val jsonObject = json.decodeFromString(JsonObject.serializer(), raw)
        val dataMap = mutableMapOf<String, JsonElement>()
        val encryptedMap = mutableMapOf<String, JsonElement>()
        val metadataMap = mutableMapOf<String, JsonElement>()

        for (entry in jsonObject) {
            val field = fields[entry.key]
            if (null === field) {
                continue
            }

            if (field.metadata) {
                metadataMap[entry.key] = entry.value
                continue
            }

            if (field.encrypted) {
                encryptedMap[entry.key] = entry.value
                continue
            }
            dataMap[entry.key] = entry.value
        }

        this.encrypt(infrastructure, json, encryptedMap, dataMap)

        return ProcessResult(
            data = json.encodeToString(JsonObject.serializer(), JsonObject(dataMap)),
            metadata = json.encodeToString(JsonObject.serializer(), JsonObject(metadataMap))
        )
    }

    fun rebuildRawJson(
        infrastructure: Infrastructure,
        json: Json,
        fields: Map<String, Setting>,
        data: String,
        metadata: String
    ): String {
        val dataObject = json.decodeFromString(JsonObject.serializer(), data)
        val metadataObject = json.decodeFromString(JsonObject.serializer(), metadata)
        val rawMap = mutableMapOf<String, JsonElement>()

        metadataObject.forEach { rawMap[it.key] = it.value }
        var hasEncryptedFields = false
        for (entry in dataObject) {
            if (entry.key == ENCRYPTED_CIPHER_ID_KEY ||
                entry.key == ENCRYPTED_ALGORITHM_KEY ||
                entry.key == ENCRYPTED_DATA_KEY
            ) {
                hasEncryptedFields = true
                continue
            }

            rawMap[entry.key] = entry.value
        }

        if (hasEncryptedFields) {
            decrypt(infrastructure, json, fields, dataObject, rawMap)
        }

        return json.encodeToString(JsonObject.serializer(), JsonObject(rawMap))
    }

    internal fun encrypt(
        infrastructure: Infrastructure,
        json: Json,
        map: Map<String, JsonElement>,
        data: MutableMap<String, JsonElement>
    ) {
        if (map.isNotEmpty()) {
            val encryptor = infrastructure.root.encryptor()
            val encrypted = encryptor.encrypt(
                json.encodeToString(JsonObject.serializer(), JsonObject(map))
            )
            data[ENCRYPTED_CIPHER_ID_KEY] = JsonPrimitive(encryptor.cipherId)
            data[ENCRYPTED_ALGORITHM_KEY] = JsonPrimitive(encryptor.algorithm)
            data[ENCRYPTED_DATA_KEY] = JsonPrimitive(encrypted)
        }
    }

    internal fun decrypt(
        infrastructure: Infrastructure,
        json: Json,
        fields: Map<String, Setting>,
        data: Map<String, JsonElement>,
        raw: MutableMap<String, JsonElement>
    ) {
        val cipherId = data[ENCRYPTED_CIPHER_ID_KEY]
        val algorithm = data[ENCRYPTED_ALGORITHM_KEY]
        val encrypted = data[ENCRYPTED_DATA_KEY]
        val encryptor = infrastructure.root.encryptor(
            cipherId = cipherId!!.toString(),
            algorithm = algorithm!!.toString()
        )

        try {
            val plain = encryptor.decrypt(encrypted!!.toString())
            val plainMap = json.decodeFromString(JsonObject.serializer(), plain)
            plainMap.forEach { raw[it.key] = it.value }
        } catch (exception: DecryptException) {
            val env = infrastructure.root.environment()
            if (!env.allowAnonymization) {
                throw exception
            }

            generateAnonymizedData(infrastructure, fields).forEach { raw[it.key] = it.value }
        }
    }

    internal fun generateAnonymizedData(
        infrastructure: Infrastructure,
        fields: Map<String, Setting>
    ): Map<String, JsonElement> {
        val faker = infrastructure.root.faker()
        val result = mutableMapOf<String, JsonElement>()
        for (field in fields) {
            if (!field.value.encrypted || field.value.faked.isEmpty()) {
                continue
            }
            val data = faker.makeFakeData(field.value.faked)
            when (data) {
                is String -> result[field.key] = JsonPrimitive(data)
                is Number -> result[field.key] = JsonPrimitive(data)
                is Boolean -> result[field.key] = JsonPrimitive(data)
            }
        }
        return result
    }
}