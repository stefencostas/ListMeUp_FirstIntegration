package com.costas.listmeup.models

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ProfileDetails(
    var itemName: String,
    var category: String,
    var acquired: Boolean,
    var quantity: Int

) {
    companion object {
        private const val LIST_KEY = "profileDetailsList"

        fun getListFromSharedPreferences(sharedPreferences: SharedPreferences): List<ProfileDetails> {
            val json = sharedPreferences.getString(LIST_KEY, "")
            return if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                Gson().fromJson(json, object : TypeToken<List<ProfileDetails>>() {}.type)
            }
        }

        fun saveListToSharedPreferences(editor: SharedPreferences.Editor, itemList: MutableList<ProfileDetails>) {
            val gson = Gson()
            val json = gson.toJson(itemList)
            editor.putString(LIST_KEY, json)
        }
    }
}