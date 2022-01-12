package com.example.nilopartner
//(3)
interface OnProductListener { //interfaz de producto
    fun onClick(product: Product)
    fun onLongClick(product: Product)
}