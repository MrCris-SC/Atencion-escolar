package com.example.atencionescolar

import VideoPlayerActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.atencionescolar.activities.DetailActivity

class ImageAdapter(
    private val context: Context,
    private var imageUrls: MutableList<String>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        Glide.with(context)
            .load(imageUrl)
            .into(holder.imageView)


        holder.imageView.setOnClickListener {
            if (imageUrl.endsWith(".mp4") || imageUrl.endsWith(".mkv")) {
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("MEDIA_URL", imageUrl)
                context.startActivity(intent)
            } else {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("MEDIA_URL", imageUrl)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = imageUrls.size

    // Método para actualizar las imágenes
    fun updateImages(newImageUrls: List<String>) {
        imageUrls.clear()
        imageUrls.addAll(newImageUrls)
        notifyDataSetChanged()
    }
}
