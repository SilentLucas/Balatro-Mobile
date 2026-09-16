package com.example.poker.model

/**
 * Representa os treze valores (ranks) de cartas no baralho.
 * [value]: Valor ordinal para comparação e ordenação (2 a 14).
 * [defaultChips]: Chips base concedidos individualmente quando a carta pontua.
 */
enum class Rank(
    val displayName: String,
    val symbol: String,
    val value: Int,
    val defaultChips: Long
) {
    TWO("Dois", "2", 2, 2L),
    THREE("Três", "3", 3, 3L),
    FOUR("Quatro", "4", 4, 4L),
    FIVE("Cinco", "5", 5, 5L),
    SIX("Seis", "6", 6, 6L),
    SEVEN("Sete", "7", 7, 7L),
    EIGHT("Oito", "8", 8, 8L),
    NINE("Nove", "9", 9, 9L),
    TEN("Dez", "10", 10, 10L),
    JACK("Valete", "J", 11, 10L),
    QUEEN("Dama", "Q", 12, 10L),
    KING("Rei", "K", 13, 10L),
    ACE("Ás", "A", 14, 11L);

    override fun toString(): String = symbol
}
