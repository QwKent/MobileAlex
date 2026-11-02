package com.example.alex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.databinding.ItemWifiBinding
import com.example.alex.util.WifiNetwork

class WifiAdapter(
    private val onClick: (WifiNetwork) -> Unit
) : RecyclerView.Adapter<WifiAdapter.VH>() {

    private val items = mutableListOf<WifiNetwork>()

    fun submit(list: List<WifiNetwork>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemWifiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemWifiBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(n: WifiNetwork) {
            b.wifiName.text = n.ssid
            val lock = if (n.secured) "🔒 Защищена" else "Открытая"
            b.wifiSub.text = "$lock · сигнал ${n.signalBars}/4"
            b.root.setOnClickListener { onClick(n) }
        }
    }
}