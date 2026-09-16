package com.example.poker.scoring

import com.example.poker.evaluator.PokerHandResult

/**
 * Calculador determinístico de pontuação com base na fórmula fundamental:
 * Score = Total Chips × Total Mult
 *
 * Ordem determinística de resolução:
 * 1. Base Chips & Base Mult da mão de pôquer avaliada.
 * 2. Adição dos Chips das cartas que formam a mão (scoringCards).
 * 3. Aplicação de modificadores aditivos de Chips (+Chips).
 * 4. Aplicação de modificadores aditivos de Mult (+Mult).
 * 5. Aplicação de modificadores multiplicativos de Mult (×Mult).
 * 6. Multiplicação final: Chips × Mult.
 */
object ScoreCalculator {

    /**
     * Calcula a pontuação detalhada de uma mão avaliada com modificadores opcionais.
     *
     * @param handResult Resultado gerado pelo [com.example.poker.evaluator.PokerHandEvaluator].
     * @param modifiers Lista de modificadores ativos nesta avaliação (preparado para Coringas futuros).
     * @param handLevel Nível atual da mão (padrão 1, permite escalabilidade futura).
     */
    fun calculateScore(
        handResult: PokerHandResult,
        modifiers: List<ScoreModifier> = emptyList(),
        handLevel: Int = 1
    ): ScoreResult {
        // Nível da mão: escala chips e mult base (no nível 1, mantém os valores padrão da mão)
        val levelMultiplier = handLevel.coerceAtLeast(1)
        val handBaseChips = handResult.baseChips * levelMultiplier
        val handBaseMult = handResult.baseMult * levelMultiplier

        // Chips provenientes exclusivamente das cartas que pontuam
        val cardsChips = handResult.scoringCards.sumOf { it.effectiveChips }

        // Modificadores de Chips
        val modifierChips = modifiers.sumOf { it.addChips() }
        val totalChips = (handBaseChips + cardsChips + modifierChips).coerceAtLeast(0L)

        // Modificadores de Mult (Aditivo e Multiplicativo)
        val additiveMult = modifiers.sumOf { it.addMult() }
        var multiplicativeMult = 1.0
        for (mod in modifiers) {
            val multFactor = mod.multiplyMult()
            if (multFactor > 0.0) {
                multiplicativeMult *= multFactor
            }
        }

        val totalMultRaw = (handBaseMult + additiveMult) * multiplicativeMult
        val totalMult = totalMultRaw.toLong().coerceAtLeast(1L)

        // Multiplicação final: Chips × Mult (com proteção de saturação para Long.MAX_VALUE)
        val finalScore = try {
            Math.multiplyExact(totalChips, totalMult)
        } catch (e: ArithmeticException) {
            Long.MAX_VALUE
        }

        return ScoreResult(
            handResult = handResult,
            baseChips = handBaseChips,
            cardsChips = cardsChips,
            modifierChips = modifierChips,
            totalChips = totalChips,
            baseMult = handBaseMult,
            modifierMult = additiveMult,
            multMultiplier = multiplicativeMult,
            totalMult = totalMult,
            finalScore = finalScore
        )
    }
}
