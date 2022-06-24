package com.example.testingmqtt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testingmqtt.MainActivity
import com.example.testingmqtt.R
import com.example.testingmqtt.databinding.ActivityGrowlightBinding
import com.example.testingmqtt.databinding.RecordJadwalBinding
import com.example.testingmqtt.models.Growlight
import com.example.testingmqtt.models.GrowlightViewModel

class GrowlightAdapter(private val onClick: (Growlight) -> Unit) :
    ListAdapter<Growlight, GrowlightAdapter.GrowlightViewHolder>(GrowlightDiffCallback)  {

    class GrowlightViewHolder(itemView : View, val onClick: (Growlight) -> Unit): RecyclerView.ViewHolder(itemView) {

        private val growlightawal: TextView = itemView.findViewById(R.id.hasilwaktu_awal)
        private val growlightakhir: TextView = itemView.findViewById(R.id.hasilwaktu_akhir)
        private var currentGrowlight: Growlight? = null

        init {
            itemView.setOnClickListener {
                currentGrowlight?.let {
                    onClick(it)
                }
            }
        }

        fun bind(growlight: Growlight) {
            currentGrowlight = growlight

            growlightawal.text = growlight.jamawal
            growlightakhir.text = growlight.jamakhir
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrowlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_jadwal, parent, false)
        return GrowlightViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: GrowlightViewHolder, position: Int) {
        val growlight = getItem(position)
        holder.bind(growlight)
    }


    
    object GrowlightDiffCallback : DiffUtil.ItemCallback<Growlight>() {
        override fun areItemsTheSame(oldItem: Growlight, newItem: Growlight): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Growlight, newItem: Growlight): Boolean {
            return oldItem.id == newItem.id
        }

    }


}