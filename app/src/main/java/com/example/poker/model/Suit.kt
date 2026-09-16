package com.example.poker.model

/**
 * Representa os quatro naipes clássicos do baralho padrão.
 * Totalmente desacoplado de qualquer engine ou framework visual.
 */
enum class Suit(val displayName: String, val symbol: String, val isRed: Boolean) {
    HEARTS("Copas", "♥", true),
    DIAMONDS("Ouros", "♦", true),
    CLUBS("Paus", "♣", false),
    SPADES("Espadas", "♠", false);

    override fun toString(): String = "$symbol $displayName"
}
