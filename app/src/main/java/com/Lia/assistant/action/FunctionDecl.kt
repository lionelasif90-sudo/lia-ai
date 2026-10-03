package com.Lia.assistant.action

import org.json.JSONArray
import org.json.JSONObject

/** Gemini function declaration; every parameter is type STRING. */
fun functionDecl(
    name: String,
    description: String,
    params: Map<String, String> = emptyMap(),
    required: List<String> = emptyList(),
): JSONObject {
    val decl = JSONObject().put("name", name).put("description", description)
    if (params.isNotEmpty()) {
        val props = JSONObject()
        params.forEach { (k, d) -> props.put(k, JSONObject().put("type", "STRING").put("description", d)) }
        val schema = JSONObject().put("type", "OBJECT").put("properties", props)
        if (required.isNotEmpty()) schema.put("required", JSONArray(required))
        decl.put("parameters", schema)
    }
    return decl
}
