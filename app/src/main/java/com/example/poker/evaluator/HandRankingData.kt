package com.example.poker.evaluator

/**
 * Estrutura determinística para desempate entre mãos do mesmo [HandType].
 * Armazena a hierarquia dos valores principais e secundários, além de kickers ordenados.
 */
data class HandRankingData(
    val primaryRankValue: Int,
    val secondaryRankValue: Int = 0,
    val kickers: List<Int> = emptyList()
) : Comparable<HandRankingData> {

    override fun compareTo(other: HandRankingData): Int {
        val primaryComp = primaryRankValue.compareTo(other.primaryRankValue)
        if (primaryComp != 0) return primaryComp

        val secondaryComp = secondaryRankValue.compareTo(other.secondaryRankValue)
        if (secondaryComp != 0) return secondaryComp

        val maxKickers = maxOf(kickers.size, other.kickers.size)
        for (i in 0 until maxKickers) {
            val thisKicker = kickers.getOrNull(i) ?: 0
            val otherKicker = other.kickers.getOrNull(i) ?: 0
            val kickerComp = thisKicker.compareTo(otherKicker)
            if (kickerComp != 0) return kickerComp
        }

        return 0
    }
}
