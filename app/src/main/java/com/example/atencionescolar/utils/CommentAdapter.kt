package com.example.atencionescolar.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atencionescolar.R
import com.example.atencionescolar.model.Comment

class CommentAdapter(private val context: Context, private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.tv_user_name)
        val commentTextView: TextView = view.findViewById(R.id.tv_comment)
        val timestampTextView: TextView = view.findViewById(R.id.tv_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.userNameTextView.text = comment.userName
        holder.commentTextView.text = comment.comment
        holder.timestampTextView.text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm", comment.timestamp)
    }

    override fun getItemCount() = comments.size

    fun addComments(commentList: List<Comment>) {
        comments.clear()
        comments.addAll(commentList)
        notifyDataSetChanged()
    }
    fun addComment(comment: Comment) {
        comments.add(comment)
        notifyItemInserted(comments.size - 1)
    }
}
