package com.example.atencionescolar

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

class AttachmentAdapter(
    private val context: Context,
    private var attachments: MutableList<Uri>
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attachmentImageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_view, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val attachmentUri = attachments[position]
        val mimeType = context.contentResolver.getType(attachmentUri)

        if (mimeType != null && mimeType.startsWith("image/")) {
            Glide.with(context)
                .load(attachmentUri)
                .into(holder.attachmentImageView)
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            // Placeholder for video thumbnail, you can use Glide or another library to generate the thumbnail
            Glide.with(context)
                .load(attachmentUri)
                .thumbnail(0.1f)
                .into(holder.attachmentImageView)
        }


    }

    override fun getItemCount() = attachments.size

    // Método para actualizar los archivos adjuntos
    fun updateAttachments(newAttachments: List<Uri>) {
        attachments.clear()
        attachments.addAll(newAttachments)
        notifyDataSetChanged()
    }
}


