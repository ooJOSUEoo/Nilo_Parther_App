package com.example.nilopartner

//primero se hace esta
data class Product( //clase para el modelo de datos de los producto
        var id: String?, //id
        var name: String?, //nombre
        var description: String?, //descripcion
        var imgUrl: String?, //url de img
        var quantity: Int = 0, // cantidad
        var price: Double = 0.0){ //precio

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
