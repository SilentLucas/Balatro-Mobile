package com.example.poker.model

/**
 * Representação imutável de uma carta de jogo individual.
 *
 * @property id Identificador único e determinístico desta instância de carta.
 * @property suit Naipe da carta.
 * @property rank Valor nominal da carta.
 * @property baseChips Quantidade de chips base dada pela carta ao pontuar.
 * @property modifiers Lista de modificadores ativos na carta.
 */
data class Card(
    val id: String,
    val suit: Suit,
    val rank: Rank,
    val baseChips: Long = rank.defaultChips,
    val modifiers: List<CardModifier> = emptyList()
) {
    /**
     * Chips totais concedidos por esta carta ao pontuar, considerando seus modificadores.
     */
    val effectiveChips: Long
        get() = baseChips + modifiers.sumOf { it.additionalChips() }

    override fun toString(): String = "${rank.symbol}${suit.symbol}"
}
