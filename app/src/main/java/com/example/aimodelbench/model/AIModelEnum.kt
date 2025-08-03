package com.example.aimodelbench.model

enum class AIModelEnum(val modelName: String,val code: String) {
    TEST("TEST.tflite", "01"),
    TEST1("logBatteryInfo", "02"),
    TEST2("logRailInfo", "03"),

    TEST3("logEnergyData", "04")
}