package com.example.atencionescolar.model


import android.os.Parcel
import android.os.Parcelable

data class Task(
    var title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<Card> = ArrayList(),
    val selected: Boolean = false // Se añade un valor por defecto para 'selected'
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.createTypedArrayList(Card.CREATOR) ?: ArrayList(), // Usamos el operador Elvis para manejar el valor nulo
        source.readByte() != 0.toByte() // Se usa readByte() para leer el valor booleano
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(createdBy)
        dest.writeTypedList(cards)
        dest.writeByte(if (selected) 1 else 0) // Se escribe el valor booleano como un byte
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)
            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }
}
