package com.example.testingmqtt.RestApi

import android.content.res.Resources
import com.example.testingmqtt.models.Growlight
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

fun GrowlightsList(resources: Resources): List<Growlight>{
    return listOf(
        Growlight(
            id = 1,
            jamawal = "12:00",
            jamakhir = "15:00"
        )

    )

}
