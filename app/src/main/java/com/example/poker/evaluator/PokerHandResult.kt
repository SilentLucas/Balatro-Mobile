package com.example.poker.evaluator

import com.example.poker.model.Card

/**
 * Resultado completo e imutável da avaliação de uma mão de cartas jogada.
 *
 * @property handType Tipo de mão identificado (ex: Full House, Flush).
 * @property scoringCards As cartas jogadas que compõem a combinação e concedem chips.
 * @property playedCards Todas as cartas submetidas para avaliação (1 a 5 cartas).
 * @property unusedCards Cartas jogadas que não fizeram parte da combinação (excedentes).
 * @property baseChips Chips base concedidos pelo tipo da mão.
 * @property baseMult Mult base concedido pelo tipo da mão.
 * @property rankingData Dados determinísticos de desempate entre mãos do mesmo tipo.
 */
data class PokerHandResult(
    val handType: HandType,
    val scoringCards: List<Card>,
    val playedCards: List<Card>,
    val unusedCards: List<Card>,
    val baseChips: Long = handType.baseChips,
    val baseMult: Long = handType.baseMult,
    val rankingData: HandRankingData
) : Comparable<PokerHandResult> {

    override fun compareTo(other: PokerHandResult): Int {
        val typeComp = handType.hierarchyScore.compareTo(other.handType.hierarchyScore)
        if (typeComp != 0) return typeComp
        return rankingData.compareTo(other.rankingData)
    }
}
