package com.example.poker.deck

import com.example.poker.model.Card
import com.example.poker.model.Rank
import com.example.poker.model.Suit
import java.util.Random

/**
 * DeckManager responsável pelo gerenciamento completo do ciclo de vida das cartas durante a Run:
 * - Baralho de compras (draw pile)
 * - Pilha de descarte (discard pile)
 * - Mão ativa de cartas em jogo
 * - Algoritmo Fisher-Yates in-place com suporte a Seed determinística
 */
class DeckManager(initialCards: List<Card>? = null) {

    private val drawPile: MutableList<Card> = mutableListOf()
    private val discardPile: MutableList<Card> = mutableListOf()

    init {
        if (initialCards != null) {
            drawPile.addAll(initialCards)
        } else {
            drawPile.addAll(createStandard52Deck())
        }
    }

    /** Quantidade de cartas disponíveis para compra */
    val remainingInDrawPile: Int
        get() = drawPile.size

    /** Quantidade de cartas na pilha de descarte */
    val remainingInDiscardPile: Int
        get() = discardPile.size

    /** Total de cartas ativas neste baralho */
    val totalCardsCount: Int
        get() = drawPile.size + discardPile.size

    /**
     * Embaralha as cartas atualmente na pilha de compra utilizando o algoritmo Fisher-Yates O(N).
     *
     * @param random Instância de Random (permite passar uma Seed determinística para a Run).
     */
    fun shuffle(random: Random = Random()) {
        val list = drawPile
        for (i in list.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
    }

    /**
     * Saca até [count] cartas do topo da pilha de compra.
     * Caso o baralho acabe, retorna todas as disponíveis.
     */
    fun draw(count: Int): List<Card> {
        val drawn = mutableListOf<Card>()
        val actualCount = count.coerceAtMost(drawPile.size)
        repeat(actualCount) {
            if (drawPile.isNotEmpty()) {
                drawn.add(drawPile.removeAt(drawPile.size - 1))
            }
        }
        return drawn
    }

    /**
     * Envia as cartas fornecidas para a pilha de descarte.
     */
    fun discard(cards: List<Card>) {
        discardPile.addAll(cards)
    }

    /**
     * Reembaralha a pilha de descarte de volta para a pilha de compra usando Fisher-Yates.
     */
    fun recycleDiscardIntoDrawPile(random: Random = Random()) {
        drawPile.addAll(discardPile)
        discardPile.clear()
        shuffle(random)
    }

    /**
     * Adiciona uma nova carta permanentemente ao baralho (ex: recompensa de loja/booster pack).
     */
    fun addCard(card: Card, toDiscard: Boolean = false) {
        if (toDiscard) {
            discardPile.add(card)
        } else {
            drawPile.add(card)
        }
    }

    /**
     * Remove uma carta permanentemente do baralho (ex: destruição por carta de Tarô).
     * Retorna true se a carta foi encontrada e removida.
     */
    fun removeCardById(cardId: String): Boolean {
        val removedFromDraw = drawPile.removeAll { it.id == cardId }
        val removedFromDiscard = discardPile.removeAll { it.id == cardId }
        return removedFromDraw || removedFromDiscard
    }

    /**
     * Reinicia o baralho para a composição clássica de 52 cartas.
     */
    fun resetToStandard() {
        drawPile.clear()
        discardPile.clear()
        drawPile.addAll(createStandard52Deck())
    }

    /**
     * Espia as próximas [count] cartas do topo da pilha de compras sem removê-las.
     */
    fun peek(count: Int): List<Card> {
        val actualCount = count.coerceAtMost(drawPile.size)
        return drawPile.takeLast(actualCount).reversed()
    }

    fun getCardsInDrawPile(): List<Card> = drawPile.toList()
    fun getCardsInDiscardPile(): List<Card> = discardPile.toList()

    companion object {
        /**
         * Gera as 52 cartas padrão (13 ranks x 4 suits), cada uma com Unique ID determinístico.
         */
        fun createStandard52Deck(): List<Card> {
            val cards = ArrayList<Card>(52)
            var index = 0
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    cards.add(
                        Card(
                            id = "std_${suit.name.lowercase()}_${rank.name.lowercase()}_$index",
                            suit = suit,
                            rank = rank,
                            baseChips = rank.defaultChips
                        )
                    )
                    index++
                }
            }
            return cards
        }
    }
}
