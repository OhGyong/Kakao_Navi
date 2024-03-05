package com.navirotation

import android.app.Application
import com.kakaomobility.knsdk.KNSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ServiceApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        // 카카오 내비
        KNSDK.install(this, "$filesDir/files")
    }
}