package com.example.testingmqtt.models

import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.MqttMessage

class SoilData {
    fun parseJson( mqttMessage: MqttMessage?): Soil? {
        var gson = Gson()
        var jsonString = mqttMessage?.payload.toString()
        var testModel = gson.fromJson(jsonString, Soil::class.java)

        return testModel

    }

}