package com.example.poker.model

/**
 * Representa uma carta de jogo individual (Playing Card) de um baralho de pôquer.
 *
 * @property uniqueId Identificador único universal e determinístico da carta (ex: "card_hearts_ace_12").
 * @property suit Naipe clássico da carta (Copas, Ouros, Paus, Espadas).
 * @property rank Valor/hierarquia ordinal da carta (2 ao Ás).
 * @property baseChips Quantidade de chips concedida individualmente por esta carta ao pontuar.
 * @property modifiers Modificadores adicionais vinculados à carta (edições, aprimoramentos, selos).
 */
data class PlayingCard(
    val uniqueId: String,
    val suit: Suit,
    val rank: Rank,
    val baseChips: Long = rank.defaultChips,
    val modifiers: List<CardModifier> = emptyList()
) {
    /**
     * Chips totais concedidos por esta carta ao pontuar, considerando modificadores ativos.
     */
    val effectiveChips: Long
        get() = baseChips + modifiers.sumOf { it.additionalChips() }

    override fun toString(): String = "${rank.symbol}${suit.symbol}"

    companion object {
        /**
         * Cria as 52 cartas que compõem um baralho padrão clássico,
         * atribuindo um [uniqueId] determinístico para cada uma.
         */
        fun createStandard52Deck(): List<PlayingCard> {
            val deck = ArrayList<PlayingCard>(52)
            var index = 0
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    deck.add(
                        PlayingCard(
                            uniqueId = "std_${suit.name.lowercase()}_${rank.name.lowercase()}_$index",
                            suit = suit,
                            rank = rank,
                            baseChips = rank.defaultChips
                        )
                    )
                    index++
                }
            }
            return deck
        }
    }
}
