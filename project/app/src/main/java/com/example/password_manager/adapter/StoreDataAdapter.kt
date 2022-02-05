package com.example.password_manager.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.password_manager.beans.StoreData
import com.example.password_manager.databinding.LayoutRvItemsBinding

class StoreDataAdapter (private val storeList: ArrayList<StoreData>) :RecyclerView.Adapter<StoreDataAdapter.StoreAdapterViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreAdapterViewHolder {
        val itemBinding = LayoutRvItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreAdapterViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: StoreAdapterViewHolder, position: Int) {
        val storeData: StoreData = storeList[position]
        holder.bind(storeData)
    }

    override fun getItemCount(): Int {
        return storeList.size
    }

    class StoreAdapterViewHolder(private val itemBinding: LayoutRvItemsBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(beanData: StoreData) {
            itemBinding.site.text = beanData.site_name
        }
    }
}