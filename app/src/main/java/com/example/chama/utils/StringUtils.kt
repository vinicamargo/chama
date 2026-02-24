package com.example.chama.utils

import java.text.Normalizer

fun String.removerAcentos(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}