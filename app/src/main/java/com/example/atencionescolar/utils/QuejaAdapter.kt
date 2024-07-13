package com.example.atencionescolar
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.atencionescolar.activities.datos_activity
import com.example.atencionescolar.model.Queja
import com.example.atencionescolar.utils.QuejaViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList
class QuejaAdapter(
    private val context: Context,
    private val quejasList: ArrayList<Queja>,
    private val userType: String // Agrega userType aquí
) : RecyclerView.Adapter<QuejaAdapter.QuejaViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuejaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.articulo_queja, parent, false)
        return QuejaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: QuejaViewHolder, position: Int) {
        val queja = quejasList[position]
        holder.bind(queja)

        // Maneja el evento de clic en la queja
        holder.itemView.setOnClickListener {
            val intent = Intent(context, datos_activity::class.java)
            intent.putExtra("documentId", queja.id) // Pasa el ID de la queja
            intent.putExtra("userType", userType) // Pasa el tipo de usuario
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return quejasList.size
    }

    fun updateList(newList: ArrayList<Queja>) {
        quejasList.clear()
        quejasList.addAll(newList)
        notifyDataSetChanged()
    }
}
