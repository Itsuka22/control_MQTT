package com.example.testingmqtt

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.testingmqtt.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import me.adawoud.bottomsheettimepicker.BottomSheetTimeRangePicker
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import zion830.com.range_picker_dialog.TimeRangePickerDialog
import zion830.com.range_picker_dialog.databinding.TimeRangePickerDialogBinding
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity(),BottomSheetTimeRangePicker.OnTimeRangeSelectedListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var textview: TextView
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private val tagBottomSheetTimeRangePicker = "tagBottomSheetTimeRangePicker"
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setMqttCallBack()
        setClickListener()

//        var button : Button = findViewById(R.id.button)
//        var buttonsub: Button = findViewById(R.id.buttonsub)


        binding.button.setOnClickListener {
            var snackbarMsg : String
            val topic = "test-mqtt"
            val msg = "Hello, world!"
            snackbarMsg = try {
                mqttClient.publish(topic, msg)
                "Published to topic '$topic'"
            } catch (ex: MqttException) {
                "Error publishing to topic: $topic"
            }
            Snackbar.make(it, snackbarMsg, 300)
                .setAction("Action", null).show()
        }

        binding.buttonsub.setOnClickListener { view ->
            var snackbarMsg : String
            val topic = "mqtt-test-sub"
            snackbarMsg = "Cannot subscribe to empty topic!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.subscribe(topic)
                    "Subscribed to topic '$topic'"
                } catch (ex: MqttException) {
                    "Error subscribing to topic: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setClickListener(){
        binding.timegrowlightFrom.setOnClickListener {
            showTimePicker()
        }
        binding.btnSet.setOnClickListener {
            showTimerange()
        }
        binding.btnOk.setOnClickListener {

        }
        binding.timegrowlightTo.setOnClickListener {
            showTimePicker2()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker() {
        val hour = selectedHour ?: LocalDateTime.now().hour
        val minute = selectedMinute ?: LocalDateTime.now().minute
        MaterialTimePicker.Builder()
            .setTitleText("Waktu GreenHouse")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
            .apply {
                addOnPositiveButtonClickListener { onTimeSelected(this.hour, this.minute) }
            }.show(supportFragmentManager, MaterialTimePicker::class.java.canonicalName)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onTimeSelected(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute

        "$hourAsText:$minuteAsText".also { findViewById<TextView>(R.id.timegrowlight_from).text = it }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker2(){
        val hour = selectedHour ?: LocalDateTime.now().hour
        val minute = selectedMinute ?: LocalDateTime.now().minute
        MaterialTimePicker.Builder()
            .setTitleText("Waktu GreenHouse")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
            .apply {
                addOnPositiveButtonClickListener { onTimeSelected2(this.hour, this.minute) }
            }.show(supportFragmentManager, MaterialTimePicker::class.java.canonicalName)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onTimeSelected2(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute

        "$hourAsText:$minuteAsText".also { findViewById<TextView>(R.id.timegrowlight_to).text = it }
    }

    fun duration(){

    }




    // INI LIBRARY BOTTOM RANGE TIME PICKER


    private fun showTimerange(){
       BottomSheetTimeRangePicker
           .newInstance(this, DateFormat.is24HourFormat(this))
           .show(supportFragmentManager, tagBottomSheetTimeRangePicker)
    }



    override fun onTimeRangeSelected(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        var startHourString = startHour.toString()
        var startMinuteString = startMinute.toString()
        var endHourString = endHour.toString()
        var endMinuteString = endMinute.toString()
        when {
            startHour < 9 -> startHourString = startHour.toString().prependZero()
            startMinute < 9 -> startMinuteString = startMinute.toString().prependZero()
            endHour < 9 -> endHourString = endHour.toString().prependZero()
            endMinute < 9 -> endMinuteString = endMinute.toString().prependZero()
        }

       binding.tvRangeTime.text= getString(
            R.string.chosen_time_range,
            startHourString,
            startMinuteString,
            endHourString,
            endMinuteString
        )
    }
    private fun String.prependZero(): String {
        return "0".plus(this)
    }


//    INI MQTT NYA
    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = "Connected to host:\n'$MQTT_HOST'."
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = "Connection to host lost:\n'$MQTT_HOST'"
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$MQTT_HOST': $mqttMessage")
                val str: String = "------------"+ Calendar.getInstance().time +"-------------\n$mqttMessage"
                textview.text = str
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$MQTT_HOST'")
            }
        })
    }

}