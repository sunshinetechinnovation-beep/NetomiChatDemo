package com.ssti.netomichatdemonogit

import android.app.Application
import com.ssti.netomichatdemonogit.util.NetworkObserver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkObserver.init(this)
    }
}