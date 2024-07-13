package com.example.atencionescolar.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.atencionescolar.R
import com.example.atencionescolar.model.Queja
import de.hdodenhof.circleimageview.CircleImageView

class QuejaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ivBoardImage: CircleImageView = itemView.findViewById(R.id.iv_board_image)
    val tvName: TextView = itemView.findViewById(R.id.tv_name)
    val tvCreatedBy: TextView = itemView.findViewById(R.id.tv_created_by)

    fun bind(queja: Queja) {
        // Asigna los datos de la queja a los elementos del ViewHolder
        tvName.text = queja.asunto
        tvCreatedBy.text = queja.descripcion

        // Configura la imagen utilizando Glide
        if (queja.imageUrls.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(queja.imageUrls[0])  // Carga la primera URL de imagen
                .apply(RequestOptions().placeholder(R.drawable.ic_profile))  // Placeholder
                .into(ivBoardImage)
        }
    }
}