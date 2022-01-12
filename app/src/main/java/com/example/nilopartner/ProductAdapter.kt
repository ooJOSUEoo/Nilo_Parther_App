package com.example.nilopartner

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.nilopartner.databinding.ItemProductBinding

class ProductAdapter(private val productList: MutableList<Product>,
                     private val listener: OnProductListener) { //adaptador de producto

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemProductBinding.bind(view)

        fun setListener(product: Product){ //cuando hay un click
            binding.root.setOnClickListener {
                listener.onClick(product)
            }

            binding.root.setOnLongClickListener { //cuando hay un click largo
                listener.onLongClick(product)
                true
            }
        }
    }

}