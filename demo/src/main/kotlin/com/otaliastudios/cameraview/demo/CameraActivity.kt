package com.otaliastudios.cameraview.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.otaliastudios.cameraview.demo.databinding.ActivityCameraBinding


class CameraActivity : AppCompatActivity() {

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val binding = DataBindingUtil.setContentView<ActivityCameraBinding>(this, R.layout.activity_camera)
        supportFragmentManager.beginTransaction().add(binding.frameLayout.id, CameraFragment())
            .commit()
    }
}
