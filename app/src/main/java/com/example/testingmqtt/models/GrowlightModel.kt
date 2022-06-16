package com.example.testingmqtt.growlight
import com.google.gson.annotations.SerializedName

data class GrowlightModel(
    @SerializedName("id") val id: String = "",
    @SerializedName("time") val jamawal: String = "",
    @SerializedName("location") val jamakhir: String = ""
)