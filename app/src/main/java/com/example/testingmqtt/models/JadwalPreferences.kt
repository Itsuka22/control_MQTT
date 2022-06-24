package com.example.testingmqtt.models

import com.chibatching.kotpref.KotprefModel


object JadwalPreferences : KotprefModel(){
    var growlightStart by stringPref()
    var growlightEnd by stringPref()

}