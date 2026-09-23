package com.najara.app.data

data class Movie(
    val title: String,
    val poster: String,
    val category: String,
    val embedLink: String,
    val downloadLink: String,
    val trailer: String = "",
    val rating: String = "",
    val print: String = "",
    val industry: String = "",
    val language: String = "",
    val quality: String = ""
)
