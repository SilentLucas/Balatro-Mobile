package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.deck.Deck
import com.example.poker.evaluator.HandType
import com.example.poker.evaluator.PokerHandEvaluator
import com.example.poker.evaluator.PokerHandResult
import com.example.poker.model.Card
import com.example.poker.model.Rank
import com.example.poker.model.Suit
import com.example.poker.scoring.ScoreCalculator
import com.example.poker.scoring.ScoreResult
import com.example.ui.theme.CardSuitBlack
import com.example.ui.theme.CardSuitRed
import com.example.ui.theme.CardTextDark
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChipBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MultRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.PokerSurfaceCard
import com.example.ui.theme.PrimaryBlue
import java.util.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_screen")
                ) { innerPadding ->
                    PokerLabScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PokerLabScreen(modifier: Modifier = Modifier) {
    // Instância do baralho e gerador determinístico
    val randomSeed = remember { 42L }
    val deck = remember {
        val d = Deck()
        d.shuffle(Random(randomSeed))
        d
    }

    var remainingInDeck by remember { mutableStateOf(deck.remainingCards) }
    var discardedCount by remember { mutableStateOf(deck.discardedCardsCount) }

    // Cartas na mão do jogador
    val currentHand = remember {
        mutableStateListOf<Card>().apply {
            addAll(deck.draw(5))
        }
    }

    // Cartas selecionadas para jogar/avaliar
    val selectedCards = remember { mutableStateListOf<Card>() }

    // Resultado da avaliação e pontuação
    var evaluationResult by remember {
        val initialCards = currentHand.take(5)
        val handRes = if (initialCards.isNotEmpty()) PokerHandEvaluator.evaluate(initialCards) else null
        val scoreRes = handRes?.let { ScoreCalculator.calculateScore(it) }
        mutableStateOf<Pair<PokerHandResult, ScoreResult>?>(
            if (handRes != null && scoreRes != null) Pair(handRes, scoreRes) else null
        )
    }

    // Inicializa seleção padrão com as primeiras cartas
    remember {
        selectedCards.addAll(currentHand)
    }

    fun triggerEvaluation(cardsToEval: List<Card>) {
        if (cardsToEval.isEmpty()) {
            evaluationResult = null
            return
        }
        val handRes = PokerHandEvaluator.evaluate(cardsToEval)
        val scoreRes = ScoreCalculator.calculateScore(handRes)
        evaluationResult = Pair(handRes, scoreRes)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PokerFeltDark, Color(0xFF090D14))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabeçalho de Status
        HeaderCard(
            remainingInDeck = remainingInDeck,
            discardedCount = discardedCount,
            seed = randomSeed
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Placar de Pontuação Dinâmico
        evaluationResult?.let { (handRes, scoreRes) ->
            ScoreBoard(handResult = handRes, scoreResult = scoreRes)
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PokerSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selecione entre 1 e 5 cartas para pontuar",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mão de Cartas Interativa
        Text(
            text = "SUA MÃO (${currentHand.size} CARTAS)",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            currentHand.forEach { card ->
                val isSelected = selectedCards.any { it.id == card.id }
                val isScoring = evaluationResult?.first?.scoringCards?.any { it.id == card.id } == true

                PokerCardView(
                    card = card,
                    isSelected = isSelected,
                    isScoring = isScoring,
                    onClick = {
                        if (isSelected) {
                            selectedCards.removeAll { it.id == card.id }
                        } else {
                            if (selectedCards.size < 5) {
                                selectedCards.add(card)
                            }
                        }
                        triggerEvaluation(selectedCards)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ações Principais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (selectedCards.isNotEmpty()) {
                        deck.discard(selectedCards.toList())
                        currentHand.removeAll(selectedCards)
                        selectedCards.clear()

                        val drawn = deck.draw(5 - currentHand.size)
                        currentHand.addAll(drawn)
                        selectedCards.addAll(currentHand)

                        remainingInDeck = deck.remainingCards
                        discardedCount = deck.discardedCardsCount
                        triggerEvaluation(selectedCards)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Jogar Mão", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    deck.resetToStandard()
                    deck.shuffle(Random(System.currentTimeMillis()))
                    currentHand.clear()
                    selectedCards.clear()
                    val drawn = deck.draw(5)
                    currentHand.addAll(drawn)
                    selectedCards.addAll(currentHand)
                    remainingInDeck = deck.remainingCards
                    discardedCount = deck.discardedCardsCount
                    triggerEvaluation(selectedCards)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("shuffle_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reembaralhar")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Presets de Teste Rápido de Mãos
        Text(
            text = "TESTE RÁPIDO DE COMBINAÇÕES",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip("Royal Flush") {
                loadPreset(
                    listOf(
                        Card("p_10", Suit.SPADES, Rank.TEN),
                        Card("p_j", Suit.SPADES, Rank.JACK),
                        Card("p_q", Suit.SPADES, Rank.QUEEN),
                        Card("p_k", Suit.SPADES, Rank.KING),
                        Card("p_a", Suit.SPADES, Rank.ACE)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Straight Flush (Wheel A-5)") {
                loadPreset(
                    listOf(
                        Card("w_a", Suit.HEARTS, Rank.ACE),
                        Card("w_2", Suit.HEARTS, Rank.TWO),
                        Card("w_3", Suit.HEARTS, Rank.THREE),
                        Card("w_4", Suit.HEARTS, Rank.FOUR),
                        Card("w_5", Suit.HEARTS, Rank.FIVE)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Quadra (4 Ases)") {
                loadPreset(
                    listOf(
                        Card("q_1", Suit.SPADES, Rank.ACE),
                        Card("q_2", Suit.HEARTS, Rank.ACE),
                        Card("q_3", Suit.CLUBS, Rank.ACE),
                        Card("q_4", Suit.DIAMONDS, Rank.ACE),
                        Card("q_5", Suit.SPADES, Rank.NINE)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Full House (K, K, K, 7, 7)") {
                loadPreset(
                    listOf(
                        Card("fh_1", Suit.SPADES, Rank.KING),
                        Card("fh_2", Suit.HEARTS, Rank.KING),
                        Card("fh_3", Suit.CLUBS, Rank.KING),
                        Card("fh_4", Suit.DIAMONDS, Rank.SEVEN),
                        Card("fh_5", Suit.SPADES, Rank.SEVEN)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Flush (Copas)") {
                loadPreset(
                    listOf(
                        Card("fl_1", Suit.HEARTS, Rank.TWO),
                        Card("fl_2", Suit.HEARTS, Rank.SIX),
                        Card("fl_3", Suit.HEARTS, Rank.EIGHT),
                        Card("fl_4", Suit.HEARTS, Rank.JACK),
                        Card("fl_5", Suit.HEARTS, Rank.ACE)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Sequência (Straight)") {
                loadPreset(
                    listOf(
                        Card("st_1", Suit.SPADES, Rank.EIGHT),
                        Card("st_2", Suit.HEARTS, Rank.NINE),
                        Card("st_3", Suit.DIAMONDS, Rank.TEN),
                        Card("st_4", Suit.CLUBS, Rank.JACK),
                        Card("st_5", Suit.SPADES, Rank.QUEEN)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }

            PresetChip("Dois Pares") {
                loadPreset(
                    listOf(
                        Card("tp_1", Suit.SPADES, Rank.JACK),
                        Card("tp_2", Suit.HEARTS, Rank.JACK),
                        Card("tp_3", Suit.DIAMONDS, Rank.FOUR),
                        Card("tp_4", Suit.CLUBS, Rank.FOUR),
                        Card("tp_5", Suit.SPADES, Rank.KING)
                    ),
                    currentHand, selectedCards
                ) { triggerEvaluation(it) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun loadPreset(
    presetCards: List<Card>,
    currentHand: MutableList<Card>,
    selectedCards: MutableList<Card>,
    onTrigger: (List<Card>) -> Unit
) {
    currentHand.clear()
    selectedCards.clear()
    currentHand.addAll(presetCards)
    selectedCards.addAll(presetCards)
    onTrigger(presetCards)
}

@Composable
fun HeaderCard(remainingInDeck: Int, discardedCount: Int, seed: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PokerSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "POKER ROGUELIKE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = GoldAccent
                )
                Text(
                    text = "Etapa 1: Core de Pôquer & Pontuação",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(label = "Deck", value = "$remainingInDeck")
                StatusBadge(label = "Descarte", value = "$discardedCount")
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PokerSurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ScoreBoard(handResult: PokerHandResult, scoreResult: ScoreResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PokerSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nome da Mão Identificada
            Surface(
                shape = RoundedCornerShape(50),
                color = when (handResult.handType) {
                    HandType.ROYAL_FLUSH, HandType.STRAIGHT_FLUSH -> Color(0xFF8B5CF6)
                    HandType.FOUR_OF_A_KIND, HandType.FULL_HOUSE -> Color(0xFFF59E0B)
                    else -> PrimaryBlue
                }
            ) {
                Text(
                    text = handResult.handType.displayName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fórmula: (Chips) × (Mult) = Score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Chips Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChipBlue
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CHIPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "${scoreResult.totalChips}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = " × ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Mult Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MultRed
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MULT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "${scoreResult.totalMult}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = " = ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Total Score
                Column(horizontalAlignment = Alignment.Start) {
                    Text("PONTUAÇÃO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text(
                        text = "${scoreResult.finalScore}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Decomposição Detalhada
            Text(
                text = "Base: ${scoreResult.baseChips}c / ${scoreResult.baseMult}m  •  Cartas: +${scoreResult.cardsChips}c  •  Pontuando: ${handResult.scoringCards.size} cartas",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PokerCardView(
    card: Card,
    isSelected: Boolean,
    isScoring: Boolean,
    onClick: () -> Unit
) {
    val suitColor = if (card.suit.isRed) CardSuitRed else CardSuitBlack
    val elevationOffset = if (isSelected) (-12).dp else 0.dp

    Box(
        modifier = Modifier
            .offset(y = elevationOffset)
            .width(62.dp)
            .height(92.dp)
            .shadow(if (isSelected) 10.dp else 4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(CardWhite)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) GoldAccent else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        // Canto Superior Esquerdo
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = card.rank.symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = suitColor,
                lineHeight = 14.sp
            )
            Text(
                text = card.suit.symbol,
                fontSize = 12.sp,
                color = suitColor,
                lineHeight = 12.sp
            )
        }

        // Símbolo Central
        Text(
            text = card.suit.symbol,
            fontSize = 28.sp,
            color = suitColor.copy(alpha = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        )

        // Chips no Rodapé
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            shape = RoundedCornerShape(4.dp),
            color = if (isScoring) ChipBlue.copy(alpha = 0.15f) else Color.Transparent
        ) {
            Text(
                text = "+${card.effectiveChips}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isScoring) ChipBlue else Color.Gray,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PokerSurfaceCard,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
