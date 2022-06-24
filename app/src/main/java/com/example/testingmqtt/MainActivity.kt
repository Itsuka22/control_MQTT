package com.example.testingmqtt


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testingmqtt.adapter.GrowlightAdapter
import com.example.testingmqtt.databinding.ActivityMainBinding
import com.example.testingmqtt.models.Growlight
import com.example.testingmqtt.models.GrowlightListViewModelFactory
import com.example.testingmqtt.models.GrowlightViewModel
import com.example.testingmqtt.models.JadwalPreferences
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.time.LocalDateTime
import kotlin.math.log


//const val GROWLIGHT_ID = "growlight id"
class MainActivity : AppCompatActivity(){
    private val newFlowerActivityRequestCode = 1
    lateinit var binding: ActivityMainBinding
    private lateinit var textview: TextView
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private lateinit var adapter: GrowlightAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val growlightListViewModel by viewModels<GrowlightViewModel> {
        GrowlightListViewModelFactory(this)
    }
    var growlights : List<GrowlightAdapter> = listOf()
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setMqttCallBack()
        setClickListener()
        initialisation()




//        BUTTON PUBLISH DAN SUSCRIBE MQTT

        binding.button.setOnClickListener {
            var snackbarMsg : String
            val topic = "inastek/growlight"
            val waktu = Growlight(jamawal = "${binding.timegrowlightFrom.text}", id = 1, jamakhir = "${binding.timegrowlightTo.text}")
            val waktugson = Gson().toJson(waktu)
            Log.d("Object to json",waktugson)
            val msg = "$waktugson"
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
            val topic = "inastek/growlight"
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

//        Dan Lain2
        binding.btnGrowlight.setOnClickListener {
            val intent = Intent(this, GrowlightActivity::class.java)
            startActivity(intent)
        }

//        untuk Kirim data recycle vie mqtt semua ke broker
        binding.btnMqtt.setOnClickListener {
            val waktu = Growlight(jamawal = "$binding", id = 1, jamakhir = "jamakhir")
            val waktugson = Gson().toJson(waktu)
            Log.d("Object to json",waktugson)
            val msg = "$waktugson"
            val topic = "inastek/growlight"
            mqttClient.publish(msg,topic)

        }


//        val flowerName = data.getStringExtra(FLOWER_NAME)
//        val flowerDescription = data.getStringExtra(FLOWER_DESCRIPTION)

//        flowersListViewModel.insertFlower(flowerName, flowerDescription)
    }





    fun tampil(){
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.rvRecordjadwal.layoutManager = linearLayoutManager
        val growlightAdapter = GrowlightAdapter { growlight -> selectedHour }
        val concatAdapter = ConcatAdapter(growlightAdapter)

        growlightListViewModel.growlightLiveData.observe(this, {
            it?.let {
                growlightAdapter.submitList(it as MutableList<Growlight>)
            }
        })
        val recyclerView: RecyclerView = binding.rvRecordjadwal
        recyclerView.adapter = concatAdapter
        val jamMulai = binding.timegrowlightFrom.text
        val jamSampai = binding.timegrowlightTo.text
//            val duration = getDurationDescription(this, Duration.ofHours(1))
        growlightListViewModel.insertWaktu(jamMulai.toString(),jamSampai.toString())
//        val timeRange = "[$jamMulai-$jamSampai]"
//        binding.tvRangeTime.text = timeRange
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun setClickListener(){
        binding.timegrowlightFrom.setOnClickListener {
            showTimePicker()
        }
        binding.btnOk.setOnClickListener {
            tampil()
        }

        binding.timegrowlightTo.setOnClickListener {
            showTimePicker2()
        }

//        meneylesaikan pengiriman mqtt

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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, waktudata: Intent?) {
//        super.onActivityResult(requestCode, resultCode, waktudata)
//        if(requestCode == newFlowerActivityRequestCode && resultCode == Activity.RESULT_OK){
//            waktudata?.let { data ->
//                val waktuawal= data.getStringExtra(WAKTU_AWAL)
//                val waktuakhir = data.getStringExtra(WAKTU_AKHIR)
//
//            GrowlightViewModel
//
//            }
//        }
//    }





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
            override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
                Log.w("Debug", "Message received from host '$MQTT_HOST': $mqttMessage")
                val str: String = "$mqttMessage"
                binding.testpesanmqtt.text = str


            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$MQTT_HOST'")
            }
        })
    }

    fun deleteJam(item: com.example.testingmqtt.models.Growlight) {


    }

    companion object{
        const val GROWLIGHT_ID = "flower id"
        const val KEYKIRIMWAKTU = "KirimWaktu"
        const val WAKTU_AKHIR = "waktu akhir"
        const val WAKTU_AWAL = "waktu awal"
    }



}