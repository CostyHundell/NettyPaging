package com.costyhundell.nettypager

import com.costyhundell.nettypager.NettyResponse
import com.google.gson.*
import java.lang.reflect.Type
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.JsonParseException
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer


class DeserializeResponse(val data:  Class<*>) : JsonDeserializer<NettyResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext
    ): NettyResponse? {
        return context.deserialize(json, data)
    }

}