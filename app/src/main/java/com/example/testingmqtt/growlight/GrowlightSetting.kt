package com.example.testingmqtt.growlight
import com.google.gson.annotations.SerializedName

data class GrowlightSetting(
    @SerializedName("timer") val timer: List<String> = listOf()
)
