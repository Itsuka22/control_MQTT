package com.example.testingmqtt.models
import com.google.gson.annotations.SerializedName

data class Growlight(
//    @SerializedName("id") val id: String = "",\
    val id: Long,
    val jamawal: String,
    val jamakhir: String
)