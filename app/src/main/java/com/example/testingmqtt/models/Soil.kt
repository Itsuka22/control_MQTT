package com.example.testingmqtt.models

import com.google.gson.Gson

class Soil (
    val ph: Int,
    val humudity: Int,
    val temperature: Double,
    val conductivity: Double,
    val nitrogen: Double,
    val phosphor: Double,
    val potasium: Double
        )