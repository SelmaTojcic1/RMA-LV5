package com.example.whereisselma

import android.app.Application

class WhereIsSelma: Application() {

    companion object {
        lateinit var instance: WhereIsSelma
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}