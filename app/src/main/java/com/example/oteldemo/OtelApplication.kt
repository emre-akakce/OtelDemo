package com.example.oteldemo

import android.app.Application
import com.example.oteldemo.otel.OtelInitializer

class OtelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OtelInitializer.init(serviceName = "android-oteldemo")
    }
}
