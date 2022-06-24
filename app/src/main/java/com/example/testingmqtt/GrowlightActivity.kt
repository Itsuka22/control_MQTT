package com.example.testingmqtt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.testingmqtt.databinding.ActivityGrowlightBinding
import org.eclipse.paho.client.mqttv3.MqttMessage

class GrowlightActivity : AppCompatActivity() {
    lateinit var binding :ActivityGrowlightBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrowlightBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mqttCal = MQTT_HOST

        val mqttMessage = MqttMessage()
//
//        binding.getHasil.setOnClickListener {
//            for (i in 0..4) {
////                subscribe(mqttMessage)
//            }


    }
}