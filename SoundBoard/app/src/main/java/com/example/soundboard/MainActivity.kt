package com.example.soundboard

import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var soundPool: SoundPool
    private var loaded = false
    var soundMap: HashMap<Int, Int> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setupUI()
        this.loadSounds()
    }

    private fun setupUI() {
        this.ib_airplane.setOnClickListener(this)
        this.ib_locomotive.setOnClickListener(this)
        this.ib_motorcycle.setOnClickListener(this)
    }

    private fun loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.soundPool = SoundPool.Builder().setMaxStreams(10).build()
        } else {
            this.soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }

        this.soundPool.setOnLoadCompleteListener { _, _, _ -> loaded = true }
        this.soundMap[R.raw.airplane] = this.soundPool.load(this, R.raw.airplane, 1)
        this.soundMap[R.raw.locomotive] = this.soundPool.load(this, R.raw.locomotive, 1)
        this.soundMap[R.raw.motorcycle] = this.soundPool.load(this, R.raw.motorcycle, 1)
    }

    override fun onClick(view: View?) {
        if(!this.loaded) return
        if (view != null) {
            when(view.id) {
                R.id.ib_airplane -> playSound(R.raw.airplane)
                R.id.ib_locomotive -> playSound(R.raw.locomotive)
                R.id.ib_motorcycle -> playSound(R.raw.motorcycle)
            }
        }
    }

    private fun playSound(selectedSound: Int) {
        val soundID = this.soundMap[selectedSound] ?: 0
        this.soundPool.play(soundID, 1f, 1f, 1, 0, 1f)
    }

}