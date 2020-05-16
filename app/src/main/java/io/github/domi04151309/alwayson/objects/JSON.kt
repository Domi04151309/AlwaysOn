package io.github.domi04151309.alwayson.objects

import org.json.JSONArray

object JSON {

    fun contains(jsonArray: JSONArray, key: String): Boolean {
        for (i in 0 until jsonArray.length()) {
            if (jsonArray.get(i) == key) return true
        }
        return false
    }

    fun remove(jsonArray: JSONArray, key: String) {
        for (i in 0 until jsonArray.length()) {
            if (jsonArray.get(i) == key) {
                jsonArray.remove(i)
                return
            }
        }
    }

    fun isEmpty(jsonArray: JSONArray): Boolean {
        return jsonArray.length() < 1
    }
}