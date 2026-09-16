package com.example.poker.model

/**
 * Interface extensível para modificadores aplicados a cartas individuais
 * (ex: edições laminadas, aprimoramentos como Carta de Aço/Vidro, selos).
 * Projetado para suportar futuras etapas sem quebra de contrato.
 */
interface CardModifier {
    val id: String
    val name: String
    fun additionalChips(): Long = 0L
    fun additionalMult(): Long = 0L
}
