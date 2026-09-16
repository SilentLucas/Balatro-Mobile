package com.example.poker

import com.example.poker.deck.Deck
import com.example.poker.evaluator.HandType
import com.example.poker.evaluator.PokerHandEvaluator
import com.example.poker.model.Card
import com.example.poker.model.Rank
import com.example.poker.model.Suit
import com.example.poker.scoring.ScoreCalculator
import com.example.poker.scoring.ScoreModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Bateria completa de testes unitários para a Etapa 1.
 * Valida o Baralho, Embaralhamento Fisher-Yates determinístico, todas as 10 mãos de pôquer,
 * casos especiais do Ás (alto e baixo A-2-3-4-5), desempates, cartas excedentes e cálculo de pontuação.
 */
class PokerSystemTest {

    private fun card(rank: Rank, suit: Suit, id: String = "${rank.symbol}_${suit.symbol}"): Card {
        return Card(id = id, suit = suit, rank = rank, baseChips = rank.defaultChips)
    }

    // =========================================================================
    // A. TESTES DO BARALHO E EMBARALHAMENTO
    // =========================================================================

    @Test
    fun `baralho padrao deve conter exatamente 52 cartas unicas com 4 naipes e 13 valores`() {
        val deck = Deck()
        assertEquals(52, deck.remainingCards)
        assertEquals(52, deck.totalCardsCount)

        val cards = deck.getCardsInDrawPile()
        val uniqueIds = cards.map { it.id }.toSet()
        assertEquals(52, uniqueIds.size)

        for (suit in Suit.values()) {
            val suitCards = cards.filter { it.suit == suit }
            assertEquals(13, suitCards.size)
        }
    }

    @Test
    fun `embaralhamento Fisher-Yates com a mesma Seed deve ser 100 por cento reproduzivel`() {
        val deckA = Deck()
        val deckB = Deck()

        // Mesma Seed de 42L
        deckA.shuffle(Random(42L))
        deckB.shuffle(Random(42L))

        val cardsA = deckA.getCardsInDrawPile().map { it.id }
        val cardsB = deckB.getCardsInDrawPile().map { it.id }

        assertEquals(cardsA, cardsB)

        // Seed diferente deve produzir ordem diferente
        val deckC = Deck()
        deckC.shuffle(Random(999L))
        val cardsC = deckC.getCardsInDrawPile().map { it.id }
        assertNotEquals(cardsA, cardsC)
    }

    @Test
    fun `compra e descarte devem atualizar quantidades corretamente`() {
        val deck = Deck()
        val drawn = deck.draw(5)

        assertEquals(5, drawn.size)
        assertEquals(47, deck.remainingCards)
        assertEquals(0, deck.discardedCardsCount)

        deck.discard(drawn)
        assertEquals(47, deck.remainingCards)
        assertEquals(5, deck.discardedCardsCount)
    }

    // =========================================================================
    // B. TESTES DAS 10 MÃOS DE PÔQUER E CASOS ESPECIAIS DO ÁS
    // =========================================================================

    @Test
    fun `deve identificar Royal Flush corretamente`() {
        val cards = listOf(
            card(Rank.TEN, Suit.SPADES),
            card(Rank.JACK, Suit.SPADES),
            card(Rank.QUEEN, Suit.SPADES),
            card(Rank.KING, Suit.SPADES),
            card(Rank.ACE, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.ROYAL_FLUSH, result.handType)
        assertEquals(5, result.scoringCards.size)
        assertEquals(0, result.unusedCards.size)
        assertEquals(Rank.ACE.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Straight Flush padrao`() {
        val cards = listOf(
            card(Rank.FIVE, Suit.HEARTS),
            card(Rank.SIX, Suit.HEARTS),
            card(Rank.SEVEN, Suit.HEARTS),
            card(Rank.EIGHT, Suit.HEARTS),
            card(Rank.NINE, Suit.HEARTS)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.STRAIGHT_FLUSH, result.handType)
        assertEquals(Rank.NINE.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Straight Flush com As baixo Wheel A-2-3-4-5 do mesmo naipe`() {
        val cards = listOf(
            card(Rank.ACE, Suit.CLUBS),
            card(Rank.TWO, Suit.CLUBS),
            card(Rank.THREE, Suit.CLUBS),
            card(Rank.FOUR, Suit.CLUBS),
            card(Rank.FIVE, Suit.CLUBS)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.STRAIGHT_FLUSH, result.handType)
        // No Wheel, o valor ordinal de topo é 5, não 14!
        assertEquals(5, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Four of a Kind Quadra e separar kicker excedente`() {
        val kicker = card(Rank.THREE, Suit.DIAMONDS)
        val cards = listOf(
            card(Rank.JACK, Suit.SPADES),
            card(Rank.JACK, Suit.HEARTS),
            card(Rank.JACK, Suit.CLUBS),
            card(Rank.JACK, Suit.DIAMONDS),
            kicker
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.FOUR_OF_A_KIND, result.handType)
        assertEquals(4, result.scoringCards.size)
        assertEquals(listOf(kicker), result.unusedCards)
        assertEquals(Rank.JACK.value, result.rankingData.primaryRankValue)
        assertEquals(listOf(Rank.THREE.value), result.rankingData.kickers)
    }

    @Test
    fun `deve identificar Full House corretamente`() {
        val cards = listOf(
            card(Rank.KING, Suit.SPADES),
            card(Rank.KING, Suit.HEARTS),
            card(Rank.KING, Suit.DIAMONDS),
            card(Rank.SEVEN, Suit.CLUBS),
            card(Rank.SEVEN, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.FULL_HOUSE, result.handType)
        assertEquals(5, result.scoringCards.size)
        assertEquals(Rank.KING.value, result.rankingData.primaryRankValue)
        assertEquals(Rank.SEVEN.value, result.rankingData.secondaryRankValue)
    }

    @Test
    fun `deve identificar Flush corretamente quando naipes sao iguais e ranks nao consecutivos`() {
        val cards = listOf(
            card(Rank.TWO, Suit.DIAMONDS),
            card(Rank.FIVE, Suit.DIAMONDS),
            card(Rank.EIGHT, Suit.DIAMONDS),
            card(Rank.JACK, Suit.DIAMONDS),
            card(Rank.ACE, Suit.DIAMONDS)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.FLUSH, result.handType)
        assertEquals(5, result.scoringCards.size)
        assertEquals(Rank.ACE.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Sequencia com As alto 10-J-Q-K-A`() {
        val cards = listOf(
            card(Rank.TEN, Suit.HEARTS),
            card(Rank.JACK, Suit.SPADES),
            card(Rank.QUEEN, Suit.DIAMONDS),
            card(Rank.KING, Suit.CLUBS),
            card(Rank.ACE, Suit.HEARTS)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.STRAIGHT, result.handType)
        assertEquals(Rank.ACE.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Sequencia Wheel com As baixo A-2-3-4-5 naipes mistos`() {
        val cards = listOf(
            card(Rank.ACE, Suit.SPADES),
            card(Rank.TWO, Suit.HEARTS),
            card(Rank.THREE, Suit.DIAMONDS),
            card(Rank.FOUR, Suit.CLUBS),
            card(Rank.FIVE, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.STRAIGHT, result.handType)
        assertEquals(5, result.rankingData.primaryRankValue)
    }

    @Test
    fun `nao deve considerar sequencia wrap-around invalida K-A-2-3-4`() {
        val cards = listOf(
            card(Rank.KING, Suit.SPADES),
            card(Rank.ACE, Suit.HEARTS),
            card(Rank.TWO, Suit.DIAMONDS),
            card(Rank.THREE, Suit.CLUBS),
            card(Rank.FOUR, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        // K-A-2-3-4 é inválido como sequência no pôquer clássico
        assertNotEquals(HandType.STRAIGHT, result.handType)
        assertEquals(HandType.HIGH_CARD, result.handType)
    }

    @Test
    fun `deve identificar Three of a Kind Trinca e separar 2 kickers excedentes`() {
        val cards = listOf(
            card(Rank.EIGHT, Suit.SPADES),
            card(Rank.EIGHT, Suit.HEARTS),
            card(Rank.EIGHT, Suit.CLUBS),
            card(Rank.KING, Suit.DIAMONDS),
            card(Rank.TWO, Suit.HEARTS)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.THREE_OF_A_KIND, result.handType)
        assertEquals(3, result.scoringCards.size)
        assertEquals(2, result.unusedCards.size)
        assertEquals(Rank.EIGHT.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar Two Pair Dois Pares e separar o kicker excedente`() {
        val kicker = card(Rank.ACE, Suit.CLUBS)
        val cards = listOf(
            card(Rank.TEN, Suit.HEARTS),
            card(Rank.TEN, Suit.SPADES),
            card(Rank.FOUR, Suit.DIAMONDS),
            card(Rank.FOUR, Suit.CLUBS),
            kicker
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.TWO_PAIR, result.handType)
        assertEquals(4, result.scoringCards.size)
        assertEquals(listOf(kicker), result.unusedCards)
        assertEquals(Rank.TEN.value, result.rankingData.primaryRankValue)
        assertEquals(Rank.FOUR.value, result.rankingData.secondaryRankValue)
    }

    @Test
    fun `deve identificar One Pair Um Par e separar 3 kickers excedentes`() {
        val cards = listOf(
            card(Rank.NINE, Suit.HEARTS),
            card(Rank.NINE, Suit.SPADES),
            card(Rank.ACE, Suit.DIAMONDS),
            card(Rank.SIX, Suit.CLUBS),
            card(Rank.THREE, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.ONE_PAIR, result.handType)
        assertEquals(2, result.scoringCards.size)
        assertEquals(3, result.unusedCards.size)
        assertEquals(Rank.NINE.value, result.rankingData.primaryRankValue)
    }

    @Test
    fun `deve identificar High Card Carta Alta`() {
        val cards = listOf(
            card(Rank.ACE, Suit.HEARTS),
            card(Rank.KING, Suit.SPADES),
            card(Rank.NINE, Suit.DIAMONDS),
            card(Rank.SEVEN, Suit.CLUBS),
            card(Rank.TWO, Suit.SPADES)
        )
        val result = PokerHandEvaluator.evaluate(cards)

        assertEquals(HandType.HIGH_CARD, result.handType)
        assertEquals(1, result.scoringCards.size)
        assertEquals(Rank.ACE, result.scoringCards[0].rank)
        assertEquals(4, result.unusedCards.size)
    }

    // =========================================================================
    // C. TESTES DE DESEMPATE (TIE-BREAKING / RANKING DATA)
    // =========================================================================

    @Test
    fun `desempate - Par de Ases deve vencer Par de Reis`() {
        val pairAces = PokerHandEvaluator.evaluate(
            listOf(
                card(Rank.ACE, Suit.HEARTS), card(Rank.ACE, Suit.SPADES),
                card(Rank.TWO, Suit.CLUBS), card(Rank.THREE, Suit.CLUBS), card(Rank.FOUR, Suit.CLUBS)
            )
        )
        val pairKings = PokerHandEvaluator.evaluate(
            listOf(
                card(Rank.KING, Suit.HEARTS), card(Rank.KING, Suit.SPADES),
                card(Rank.TWO, Suit.CLUBS), card(Rank.THREE, Suit.CLUBS), card(Rank.FOUR, Suit.CLUBS)
            )
        )
        assertTrue(pairAces > pairKings)
    }

    @Test
    fun `desempate - Sequencia 6-alto deve vencer Sequencia Wheel 5-alto`() {
        val sixHigh = PokerHandEvaluator.evaluate(
            listOf(
                card(Rank.TWO, Suit.SPADES), card(Rank.THREE, Suit.HEARTS),
                card(Rank.FOUR, Suit.DIAMONDS), card(Rank.FIVE, Suit.CLUBS), card(Rank.SIX, Suit.SPADES)
            )
        )
        val wheel = PokerHandEvaluator.evaluate(
            listOf(
                card(Rank.ACE, Suit.SPADES), card(Rank.TWO, Suit.HEARTS),
                card(Rank.THREE, Suit.DIAMONDS), card(Rank.FOUR, Suit.CLUBS), card(Rank.FIVE, Suit.SPADES)
            )
        )
        assertTrue(sixHigh > wheel)
    }

    // =========================================================================
    // D. TESTES DE PONTUAÇÃO (CHIPS × MULT)
    // =========================================================================

    @Test
    fun `pontuacao base sem modificadores deve somar base chips da mao mais chips das cartas que pontuam`() {
        // Par de Reis: Base Par = 10 Chips, 2 Mult.
        // Cada Rei (Face Card) concede 10 chips base.
        // Total Chips = 10 (base da mão) + 10 (Rei 1) + 10 (Rei 2) = 30 Chips.
        // Total Mult = 2 Mult.
        // Score esperado = 30 * 2 = 60 Pontos.
        val cards = listOf(
            card(Rank.KING, Suit.HEARTS),
            card(Rank.KING, Suit.SPADES),
            card(Rank.TWO, Suit.CLUBS),
            card(Rank.THREE, Suit.DIAMONDS),
            card(Rank.FOUR, Suit.SPADES)
        )
        val handResult = PokerHandEvaluator.evaluate(cards)
        val score = ScoreCalculator.calculateScore(handResult)

        assertEquals(10L, score.baseChips)
        assertEquals(20L, score.cardsChips) // 10 + 10
        assertEquals(30L, score.totalChips)
        assertEquals(2L, score.totalMult)
        assertEquals(60L, score.finalScore)
    }

    @Test
    fun `pontuacao deve suportar modificadores aditivos e multiplicativos futuros`() {
        val cards = listOf(
            card(Rank.ACE, Suit.HEARTS),
            card(Rank.ACE, Suit.SPADES),
            card(Rank.ACE, Suit.CLUBS),
            card(Rank.FIVE, Suit.DIAMONDS),
            card(Rank.SIX, Suit.SPADES)
        )
        // Trinca de Ases: Base Trinca = 30 Chips, 3 Mult.
        // 3 Ases = 11 * 3 = 33 Chips de cartas.
        // Total Chips das cartas = 33.
        // Base = 30 Chips / 3 Mult.
        // Modificador teste: +50 Chips, +4 Mult, x2.0 Mult.
        val testModifier = object : ScoreModifier {
            override val id = "mod_test_joker"
            override val name = "Coringa de Teste"
            override fun addChips(): Long = 50L
            override fun addMult(): Long = 4L
            override fun multiplyMult(): Double = 2.0
        }

        val handResult = PokerHandEvaluator.evaluate(cards)
        val score = ScoreCalculator.calculateScore(handResult, listOf(testModifier))

        // Total Chips = 30 (base) + 33 (cartas) + 50 (mod) = 113 Chips
        assertEquals(113L, score.totalChips)

        // Mult = (3 base + 4 mod) * 2.0 = 14 Mult
        assertEquals(14L, score.totalMult)

        // Final Score = 113 * 14 = 1582
        assertEquals(1582L, score.finalScore)
    }
}
