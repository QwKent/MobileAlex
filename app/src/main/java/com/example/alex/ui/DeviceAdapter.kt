package com.example.alex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.bluetooth.AlexDevice
import com.example.alex.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onClick: (AlexDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {

    private val items = mutableListOf<AlexDevice>()

    fun submit(list: List<AlexDevice>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemDeviceBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(device: AlexDevice) {
            b.deviceName.text = device.name
            b.deviceStatus.text = "Сигнал: ${device.signalLevel}/4"
            b.root.setOnClickListener { onClick(device) }
        }
    }
}