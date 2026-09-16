package com.example.poker.scoring

/**
 * Interface extensível para modificadores de pontuação de rodada.
 * Preparada para suportar Coringas, Vouchers e bônus globais em etapas futuras
 * sem alterar a lógica do calculador.
 */
interface ScoreModifier {
    val id: String
    val name: String
    fun addChips(): Long = 0L
    fun addMult(): Long = 0L
    fun multiplyMult(): Double = 1.0
}
