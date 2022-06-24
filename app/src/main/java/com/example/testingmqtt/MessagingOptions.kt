package com.example.testingmqtt

const val CLIENT_USER_NAME = "emqx"
const val CLIENT_PASSWORD = "public"
const val MQTT_HOST = "tcp://broker.emqx.io:1883"
// Other options
const val CONNECTION_TIMEOUT = 3
const val CONNECTION_KEEP_ALIVE_INTERVAL = 60
const val CONNECTION_CLEAN_SESSION = true
const val CONNECTION_RECONNECT = true