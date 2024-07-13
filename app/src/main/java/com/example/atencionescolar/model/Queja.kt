package com.example.atencionescolar.model

data class Queja(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var asunto: String = "",
    var descripcion: String = "",
    var imageUrls: List<String> = emptyList(),
    var comments: MutableList<Comment> = mutableListOf() // Lista de comentarios
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", "", "", "", emptyList(), mutableListOf())
}

data class Comment(
    var commentId: String = "",
    var userId: String = "",
    var userName: String = "",
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", 0)
}
