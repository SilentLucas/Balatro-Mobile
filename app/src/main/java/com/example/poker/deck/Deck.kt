package com.example.poker.deck

import com.example.poker.model.Card
import com.example.poker.model.Rank
import com.example.poker.model.Suit
import java.util.Random

/**
 * Gerenciador do baralho de cartas da Run.
 * Fornece criação do baralho padrão (52 cartas), embaralhamento determinístico
 * via algoritmo Fisher-Yates, saque (draw) e descarte.
 */
class Deck(initialCards: List<Card>? = null) {

    private val drawPile: MutableList<Card> = mutableListOf()
    private val discardPile: MutableList<Card> = mutableListOf()

    init {
        if (initialCards != null) {
            drawPile.addAll(initialCards)
        } else {
            drawPile.addAll(createStandard52Deck())
        }
    }

    /**
     * Quantidade de cartas disponíveis para saque no momento.
     */
    val remainingCards: Int
        get() = drawPile.size

    /**
     * Quantidade de cartas atualmente na pilha de descarte.
     */
    val discardedCardsCount: Int
        get() = discardPile.size

    /**
     * Total absoluto de cartas gerenciadas por este baralho.
     */
    val totalCardsCount: Int
        get() = drawPile.size + discardPile.size

    /**
     * Embaralha a pilha de compras utilizando o algoritmo de Fisher-Yates.
     * Permite fornecer uma instância de [Random] com Seed fixa para garantia
     * de determinismo e reprodutibilidade na Run.
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
     * Saca até [count] cartas do topo da pilha de compras.
     * Se houver menos cartas disponíveis do que [count], saca todas as restantes.
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
     * Adiciona cartas à pilha de descarte.
     */
    fun discard(cards: List<Card>) {
        discardPile.addAll(cards)
    }

    /**
     * Visualiza as próximas [count] cartas sem removê-las do baralho.
     */
    fun peek(count: Int): List<Card> {
        val actualCount = count.coerceAtMost(drawPile.size)
        return drawPile.takeLast(actualCount).reversed()
    }

    /**
     * Reinicia o baralho com 52 cartas padrão limpas e esvazia a pilha de descarte.
     */
    fun resetToStandard() {
        drawPile.clear()
        discardPile.clear()
        drawPile.addAll(createStandard52Deck())
    }

    /**
     * Retorna uma cópia imutável da lista de cartas atuais no baralho de saque.
     */
    fun getCardsInDrawPile(): List<Card> = drawPile.toList()

    /**
     * Retorna uma cópia imutável da lista de cartas no descarte.
     */
    fun getCardsInDiscardPile(): List<Card> = discardPile.toList()

    companion object {
        /**
         * Cria o baralho padrão de 52 cartas sem repetições.
         * Atribui Unique IDs previsíveis e imutáveis para rastreabilidade.
         */
        fun createStandard52Deck(): List<Card> {
            val deck = ArrayList<Card>(52)
            var cardIndex = 0
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    deck.add(
                        Card(
                            id = "std_${suit.name.lowercase()}_${rank.name.lowercase()}_$cardIndex",
                            suit = suit,
                            rank = rank,
                            baseChips = rank.defaultChips
                        )
                    )
                    cardIndex++
                }
            }
            return deck
        }
    }
}
