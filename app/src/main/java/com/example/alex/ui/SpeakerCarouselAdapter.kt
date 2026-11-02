package com.example.alex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.databinding.ItemSpeakerBinding

class SpeakerCarouselAdapter(
    private val onClick: (AlexSpeaker) -> Unit
) : RecyclerView.Adapter<SpeakerCarouselAdapter.VH>() {

    private val items = mutableListOf<AlexSpeaker>()

    fun submit(list: List<AlexSpeaker>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSpeakerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemSpeakerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: AlexSpeaker) {
            b.speakerName.text = s.name
            b.speakerSub.text = if (s.ssid.isEmpty()) "Wi-Fi не указан" else s.ssid
            b.speakerStatus.text = if (s.online) "Онлайн" else "Офлайн"
            b.speakerStatus.setTextColor(
                b.root.context.getColor(
                    if (s.online) com.example.alex.R.color.success
                    else com.example.alex.R.color.text_secondary
                )
            )
            b.dotOnline.setBackgroundResource(
                if (s.online) com.example.alex.R.drawable.bg_dot_online
                else com.example.alex.R.drawable.bg_dot_offline
            )
            b.root.setOnClickListener { onClick(s) }
        }
    }
}