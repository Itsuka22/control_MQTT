package com.example.testingmqtt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import com.example.testingmqtt.databinding.ActivityMainBinding
import com.example.testingmqtt.growlight.SettingAbsenBottomSheet
import com.example.testingmqtt.models.JadwalPreferences
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
import java.lang.reflect.Parameter
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.log
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class MainActivity : AppCompatActivity(){

    lateinit var binding: ActivityMainBinding
    private lateinit var textview: TextView
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
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
        initialisation()

//        var button : Button = findViewById(R.id.button)
//        var buttonsub: Button = findViewById(R.id.buttonsub)





        binding.button.setOnClickListener {
            var snackbarMsg : String
            val topic = "test-mqtt"
            val msg = binding.tvRangeTime.text.toString()
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
        binding.btnOk.setOnClickListener {
            val jamMulai = binding.timegrowlightFrom.text
            val jamSampai = binding.timegrowlightTo.text
            val duration = getDurationDescription(this, Duration.ofHours(1))
            val timeRange = "[$jamMulai-$jamSampai] $duration"
            binding.tvRangeTime.text = timeRange
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

    fun initialisation(){
        val SettingGrowlight = JadwalPreferences

//        binding.btnSet.setOnClickListener {
//            val bundle = bundleOf(KEYKIRIMWAKTU to "Senin")
//            print(bundle)
//        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getDurationDescription(context: Context, duration: Duration): String {
        if (duration.isNegative) {
            return "-(${getDurationDescription(context, duration.negated())})"
        }
        val daysPart = duration.toDays().toInt()
        val hoursPart = duration.toHours().toInt() % 24
        val minutesPart = duration.toMinutes().toInt() % 60
        return if (daysPart > 0) {
            context.getString(
                R.string.duration_format_days_hours_minutes,
                daysPart,
                context.resources.getQuantityString(R.plurals.days, daysPart),
                hoursPart,
                context.resources.getQuantityString(R.plurals.hours, hoursPart),
                minutesPart,
                context.resources.getQuantityString(R.plurals.minutes, minutesPart)
            )
        } else if (hoursPart > 0) {
            context.getString(
                R.string.duration_format_hours_minutes,
                hoursPart,
                context.resources.getQuantityString(R.plurals.hours, hoursPart),
                minutesPart,
                context.resources.getQuantityString(R.plurals.minutes, minutesPart)
            )
        } else {
            context.getString(
                R.string.duration_format_minutes,
                minutesPart,
                context.resources.getQuantityString(R.plurals.minutes, minutesPart)
            )
        }
    }




    // INI LIBRARY BOTTOM RANGE TIME PICKER




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

    companion object{
        const val KEYKIRIMWAKTU = "KirimWaktu"
    }



}