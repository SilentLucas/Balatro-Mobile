package com.example.poker.evaluator

import com.example.poker.model.Card
import com.example.poker.model.Rank

/**
 * Avaliador determinístico de mãos de pôquer puro.
 *
 * Avalia de 1 a 5 cartas submetidas, identifica a maior mão possível,
 * trata casos especiais de Ás (alto e baixo A-2-3-4-5), separa cartas
 * que formam a mão (scoringCards) de cartas excedentes (unusedCards),
 * e gera dados determinísticos de desempate (RankingData).
 */
object PokerHandEvaluator {

    /**
     * Avalia uma lista de cartas (1 a 5 cartas) e retorna o [PokerHandResult] correspondente.
     *
     * @throws IllegalArgumentException se a lista estiver vazia ou tiver mais de 5 cartas.
     */
    fun evaluate(cards: List<Card>): PokerHandResult {
        require(cards.isNotEmpty()) { "A mão avaliada não pode ser vazia." }
        require(cards.size <= 5) { "Uma mão de pôquer avalia no máximo 5 cartas. Recebido: ${cards.size}" }

        // Mãos com menos de 5 cartas têm escopo reduzido
        if (cards.size < 5) {
            return evaluatePartialHand(cards)
        }

        // Avaliação de mão completa de 5 cartas
        val rankGroups = cards.groupBy { it.rank }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Rank, List<Card>>> { it.value.size }
                    .thenByDescending { it.key.value }
            )

        val isFlush = cards.all { it.suit == cards[0].suit }
        val straightCheck = checkStraight(cards)

        // 1. Royal Flush: Straight Flush 10-J-Q-K-A do mesmo naipe
        if (isFlush && straightCheck.isStraight && straightCheck.highRankValue == Rank.ACE.value) {
            return PokerHandResult(
                handType = HandType.ROYAL_FLUSH,
                scoringCards = cards,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = Rank.ACE.value,
                    kickers = emptyList()
                )
            )
        }

        // 2. Straight Flush: Sequência do mesmo naipe (incluindo A-2-3-4-5)
        if (isFlush && straightCheck.isStraight) {
            return PokerHandResult(
                handType = HandType.STRAIGHT_FLUSH,
                scoringCards = cards,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = straightCheck.highRankValue,
                    kickers = emptyList()
                )
            )
        }

        // 3. Four of a Kind (Quadra): 4 cartas do mesmo rank
        if (rankGroups[0].value.size == 4) {
            val quadCards = rankGroups[0].value
            val kickerCard = rankGroups[1].value
            return PokerHandResult(
                handType = HandType.FOUR_OF_A_KIND,
                scoringCards = quadCards,
                playedCards = cards,
                unusedCards = kickerCard,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    kickers = listOf(rankGroups[1].key.value)
                )
            )
        }

        // 4. Full House: Trinca + Par
        if (rankGroups[0].value.size == 3 && rankGroups[1].value.size == 2) {
            return PokerHandResult(
                handType = HandType.FULL_HOUSE,
                scoringCards = cards,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    secondaryRankValue = rankGroups[1].key.value
                )
            )
        }

        // 5. Flush: 5 cartas do mesmo naipe
        if (isFlush) {
            val sortedValues = cards.map { it.rank.value }.sortedDescending()
            return PokerHandResult(
                handType = HandType.FLUSH,
                scoringCards = cards,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = sortedValues.first(),
                    kickers = sortedValues.drop(1)
                )
            )
        }

        // 6. Straight (Sequência): 5 cartas consecutivas de naipes variados
        if (straightCheck.isStraight) {
            return PokerHandResult(
                handType = HandType.STRAIGHT,
                scoringCards = cards,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = straightCheck.highRankValue,
                    kickers = emptyList()
                )
            )
        }

        // 7. Three of a Kind (Trinca): 3 cartas do mesmo rank
        if (rankGroups[0].value.size == 3) {
            val trioCards = rankGroups[0].value
            val unused = cards.filterNot { trioCards.contains(it) }
            val kickers = unused.map { it.rank.value }.sortedDescending()
            return PokerHandResult(
                handType = HandType.THREE_OF_A_KIND,
                scoringCards = trioCards,
                playedCards = cards,
                unusedCards = unused,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    kickers = kickers
                )
            )
        }

        // 8. Two Pair (Dois Pares): 2 pares de ranks distintos
        if (rankGroups[0].value.size == 2 && rankGroups[1].value.size == 2) {
            val highPair = rankGroups[0].value
            val lowPair = rankGroups[1].value
            val pairsCards = highPair + lowPair
            val unused = cards.filterNot { pairsCards.contains(it) }
            val kickerValue = rankGroups[2].key.value
            return PokerHandResult(
                handType = HandType.TWO_PAIR,
                scoringCards = pairsCards,
                playedCards = cards,
                unusedCards = unused,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    secondaryRankValue = rankGroups[1].key.value,
                    kickers = listOf(kickerValue)
                )
            )
        }

        // 9. One Pair (Par): 1 par de mesmo rank
        if (rankGroups[0].value.size == 2) {
            val pairCards = rankGroups[0].value
            val unused = cards.filterNot { pairCards.contains(it) }
            val kickers = unused.map { it.rank.value }.sortedDescending()
            return PokerHandResult(
                handType = HandType.ONE_PAIR,
                scoringCards = pairCards,
                playedCards = cards,
                unusedCards = unused,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    kickers = kickers
                )
            )
        }

        // 10. High Card (Carta Alta): nenhuma combinação
        val highestCard = cards.maxByOrNull { it.rank.value }!!
        val unused = cards.filterNot { it.id == highestCard.id }
        val sortedKickers = unused.map { it.rank.value }.sortedDescending()
        return PokerHandResult(
            handType = HandType.HIGH_CARD,
            scoringCards = listOf(highestCard),
            playedCards = cards,
            unusedCards = unused,
            rankingData = HandRankingData(
                primaryRankValue = highestCard.rank.value,
                kickers = sortedKickers
            )
        )
    }

    /**
     * Avalia mãos parciais contendo de 1 a 4 cartas.
     */
    private fun evaluatePartialHand(cards: List<Card>): PokerHandResult {
        val rankGroups = cards.groupBy { it.rank }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Rank, List<Card>>> { it.value.size }
                    .thenByDescending { it.key.value }
            )

        // Quadra com 4 cartas
        if (rankGroups[0].value.size == 4) {
            return PokerHandResult(
                handType = HandType.FOUR_OF_A_KIND,
                scoringCards = rankGroups[0].value,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(primaryRankValue = rankGroups[0].key.value)
            )
        }

        // Trinca com 3 ou 4 cartas
        if (rankGroups[0].value.size == 3) {
            val trio = rankGroups[0].value
            val unused = cards.filterNot { trio.contains(it) }
            return PokerHandResult(
                handType = HandType.THREE_OF_A_KIND,
                scoringCards = trio,
                playedCards = cards,
                unusedCards = unused,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    kickers = unused.map { it.rank.value }.sortedDescending()
                )
            )
        }

        // Dois Pares com 4 cartas
        if (rankGroups.size >= 2 && rankGroups[0].value.size == 2 && rankGroups[1].value.size == 2) {
            val scoring = rankGroups[0].value + rankGroups[1].value
            return PokerHandResult(
                handType = HandType.TWO_PAIR,
                scoringCards = scoring,
                playedCards = cards,
                unusedCards = emptyList(),
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    secondaryRankValue = rankGroups[1].key.value
                )
            )
        }

        // Um Par com 2, 3 ou 4 cartas
        if (rankGroups[0].value.size == 2) {
            val pair = rankGroups[0].value
            val unused = cards.filterNot { pair.contains(it) }
            return PokerHandResult(
                handType = HandType.ONE_PAIR,
                scoringCards = pair,
                playedCards = cards,
                unusedCards = unused,
                rankingData = HandRankingData(
                    primaryRankValue = rankGroups[0].key.value,
                    kickers = unused.map { it.rank.value }.sortedDescending()
                )
            )
        }

        // Carta Alta
        val highest = cards.maxByOrNull { it.rank.value }!!
        val unused = cards.filterNot { it.id == highest.id }
        return PokerHandResult(
            handType = HandType.HIGH_CARD,
            scoringCards = listOf(highest),
            playedCards = cards,
            unusedCards = unused,
            rankingData = HandRankingData(
                primaryRankValue = highest.rank.value,
                kickers = unused.map { it.rank.value }.sortedDescending()
            )
        )
    }

    /**
     * Valida se as 5 cartas formam uma sequência e determina o rank de referência.
     * Trata o Ás alto (10, J, Q, K, A) e o Ás baixo (A, 2, 3, 4, 5 - Wheel).
     * Rejeita sequências inválidas (ex: K, A, 2, 3, 4 não é sequência).
     */
    private fun checkStraight(cards: List<Card>): StraightCheckResult {
        if (cards.size != 5) return StraightCheckResult(false, 0)

        val uniqueRanks = cards.map { it.rank.value }.distinct().sortedDescending()
        if (uniqueRanks.size != 5) return StraightCheckResult(false, 0)

        // Caso Especial: Ás baixo (A-2-3-4-5 / Wheel).
        // Ranks ordenados decrescentes: [14, 5, 4, 3, 2].
        if (uniqueRanks == listOf(14, 5, 4, 3, 2)) {
            // Em A-2-3-4-5, a carta mais alta da sequência é o 5!
            return StraightCheckResult(isStraight = true, highRankValue = 5)
        }

        // Sequência padrão: a diferença entre a maior e a menor carta é exatamente 4
        val isConsecutive = (uniqueRanks[0] - uniqueRanks[4] == 4)
        return if (isConsecutive) {
            StraightCheckResult(isStraight = true, highRankValue = uniqueRanks[0])
        } else {
            StraightCheckResult(isStraight = false, 0)
        }
    }

    private data class StraightCheckResult(
        val isStraight: Boolean,
        val highRankValue: Int
    )
}
