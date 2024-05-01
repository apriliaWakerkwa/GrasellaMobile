package com.example.grasella.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grasella.Model.ModelPulsa
import com.example.grasella.R

class PulsaAdapter (private val listPulsa : ArrayList<ModelPulsa>) :
    RecyclerView.Adapter<PulsaAdapter.ViewHolder>(){

    private lateinit var nListerner : onItemClickListener

    interface  onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        nListerner = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PulsaAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.riwayat,parent,false)
        return ViewHolder(itemView, nListerner)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPulsa = listPulsa[position]
        holder.tvhp.text = currentPulsa.nomorPulsa
        holder.tvop.text = currentPulsa.operator
        holder.tvpul.text = currentPulsa.nominal
    }
    override fun getItemCount(): Int {
        return listPulsa.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val tvhp : TextView = itemView.findViewById(R.id.tvHp)
        val tvop : TextView = itemView.findViewById(R.id.tvOperator)
        val tvpul : TextView = itemView.findViewById(R.id.tvPulsa)

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
}