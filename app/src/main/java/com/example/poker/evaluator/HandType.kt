package com.example.poker.evaluator

/**
 * Classificação canônica das mãos de pôquer em ordem crescente de força e raridade.
 *
 * @property displayName Nome em português para exibição e UI.
 * @property baseChips Chips padrão concedidos pela mão no Nível 1.
 * @property baseMult Mult padrão concedido pela mão no Nível 1.
 * @property hierarchyScore Valor ordinal absoluto para comparação de categorias de mãos.
 */
enum class HandType(
    val displayName: String,
    val baseChips: Long,
    val baseMult: Long,
    val hierarchyScore: Int
) {
    HIGH_CARD("Carta Alta", 5L, 1L, 1),
    ONE_PAIR("Par", 10L, 2L, 2),
    TWO_PAIR("Dois Pares", 20L, 2L, 3),
    THREE_OF_A_KIND("Trinca", 30L, 3L, 4),
    STRAIGHT("Sequência", 30L, 4L, 5),
    FLUSH("Flush", 35L, 4L, 6),
    FULL_HOUSE("Full House", 40L, 4L, 7),
    FOUR_OF_A_KIND("Quadra", 60L, 7L, 8),
    STRAIGHT_FLUSH("Straight Flush", 100L, 8L, 9),
    ROYAL_FLUSH("Royal Flush", 100L, 8L, 10);

    override fun toString(): String = displayName
}
