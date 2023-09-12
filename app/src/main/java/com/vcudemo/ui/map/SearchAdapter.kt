package com.vcudemo.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vcudemo.data.map.SearchPlaceResponse.Document
import com.vcudemo.databinding.ItemSearchBinding

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private var itemList: ArrayList<Document> = ArrayList()
    class ViewHolder(private val binding: ItemSearchBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(document: Document) {
            if(!document.distance.contains("km")) {
                val count = document.distance.toLong()
                document.distance = "${(count/ 1000)}.${(count % 1000 / 100)}km"
            }
            binding.searchItem = document
        }
    }

    fun setItem(pItemList: ArrayList<Document>) {
        itemList = pItemList
        notifyItemRangeChanged(0, itemList.size)
//        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
}