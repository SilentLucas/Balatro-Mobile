package com.example.poker.scoring

import com.example.poker.evaluator.PokerHandResult

/**
 * Encapsula o resultado detalhado e decomposto do cálculo de pontuação.
 * Fornece todos os valores intermediários para alimentar animações de contadores,
 * feedback visual e auditoria em testes.
 */
data class ScoreResult(
    val handResult: PokerHandResult,
    val baseChips: Long,
    val cardsChips: Long,
    val modifierChips: Long,
    val totalChips: Long,
    val baseMult: Long,
    val modifierMult: Long,
    val multMultiplier: Double,
    val totalMult: Long,
    val finalScore: Long
) {
    /**
     * Resumo formatado do cálculo: ex: "([30 base + 21 cartas] = 51 Chips) × ([4 base] = 4 Mult) = 204 Pontos"
     */
    val breakdownDescription: String
        get() = buildString {
            append("($totalChips Chips) × ($totalMult Mult) = $finalScore Pontos")
            append(" [Mão: ${handResult.handType.displayName}, ")
            append("Base: ${baseChips}c/${baseMult}m, ")
            append("Cartas: +${cardsChips}c]")
        }
}
