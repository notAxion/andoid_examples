package com.example.android.dessertpusher

import android.app.Application
import timber.log.Timber

// 2. create this application class
class PusherApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

    }
}