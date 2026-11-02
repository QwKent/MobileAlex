package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alex.databinding.ActivityRoomBinding

class RoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomBinding
    private var selectedRoom: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.roomLiving.setOnClickListener { selectRoom(it, "Гостиная") }
        binding.roomKitchen.setOnClickListener { selectRoom(it, "Кухня") }
        binding.roomBedroom.setOnClickListener { selectRoom(it, "Спальня") }
        binding.roomOther.setOnClickListener { selectRoom(it, "Другая") }

        binding.btnDone.setOnClickListener {
            val room = selectedRoom.ifEmpty { "Дом" }
            val result = Intent()
            result.putExtra("room", room)
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private fun selectRoom(view: android.view.View, name: String) {
        selectedRoom = name
        val all = listOf(binding.roomLiving, binding.roomKitchen, binding.roomBedroom, binding.roomOther)
        all.forEach { it.setBackgroundResource(com.example.alex.R.drawable.bg_card) }
        view.setBackgroundResource(com.example.alex.R.drawable.bg_card_selected)
        binding.btnDone.isEnabled = true
    }
}