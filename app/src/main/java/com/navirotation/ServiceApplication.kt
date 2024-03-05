package com.navirotation

import android.app.Application
import com.kakaomobility.knsdk.KNSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ServiceApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        // KNSDK 등록
        KNSDK.install(this, "$filesDir/files")
    }
}