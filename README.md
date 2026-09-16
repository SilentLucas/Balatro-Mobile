# 🃏 Poker Roguelike Deckbuilder

> Um jogo mobile de cartas roguelike deckbuilder inspirado na mecânica de pôquer, focado em gerenciamento de recursos, criação de sinergias exponenciais e jogabilidade fluida em orientação retrato (9:16).

---

## 📱 1. Visão Geral e Conceito

* **Gênero:** Roguelike Deckbuilder / Poker-based Strategy
* **Plataforma:** Android (com arquitetura de domínio portável para iOS / KMP / Engines)
* **Orientação:** Retrato / Vertical (9:16)
* **Público-Alvo:** Mobile gamer que busca sessões táticas rápidas ou runs estratégicas profundas.
* **Core Loop:**
  $$\text{Iniciar Run} \longrightarrow \text{Escolher Blind} \longrightarrow \text{Jogar Mãos \& Pontuar} \longrightarrow \text{Vencer Blind} \longrightarrow \text{Loja \& Melhorias} \longrightarrow \text{Escalar Dificuldade}$$

---

## 🛠️ 2. Tecnologias Utilizadas

O projeto foi estruturado seguindo os mais modernos padrões de engenharia Android e arquitetura limpa:

* **Linguagem:** [Kotlin 2.0+](https://kotlinlang.org/) — Segurança de tipos estrita, imutabilidade e corrotinas.
* **Interface de Usuário (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) com **Material Design 3 (M3)** — Layout declarativo, responsivo para proporção 9:16 e otimizado com recomposição inteligente.
* **Persistência Local:** [Android Room Database](https://developer.android.com/training/data-storage/room) — SQLite local com KSP para histórico de partidas, estatísticas, seeds salvas e estados de runs suspensas.
* **Serviços em Nuvem:** [Firebase](https://firebase.google.com/) — Autenticação de jogadores, backup seguro de progresso e App Check.
* **Testes Automatizados:** 
  * [JUnit 4 & Kotlin Test](https://junit.org/) — Validação exaustiva da matemática do baralho e das regras de pôquer.
  * [Robolectric & Roborazzi](https://github.com/takahirom/roborazzi) — Testes de regressão visual e Critical User Journeys (CUJs) locais na JVM sem necessidade de emulador físico.

---

## 🏗️ 3. Arquitetura do Software

O sistema adota os princípios de **Clean Architecture** e **Data-Driven Design**, dividindo o código em camadas com baixo acoplamento:

```text
app/src/main/java/com/example/
├── poker/                          # 🧠 CAMADA DE DOMÍNIO (Kotlin Puro, Zero dependência de Android)
│   ├── model/                      # Modelos fundamentais imutáveis
│   │   ├── Suit.kt                 # Naipes (Copas, Ouros, Paus, Espadas)
│   │   ├── Rank.kt                 # Valores ordinais (2 até Ás) e chips padrão
│   │   ├── Card.kt                 # Entidade imutável da carta e cálculo de chips efetivos
│   │   └── CardModifier.kt         # Contrato extensível para modificadores de cartas
│   │
│   ├── deck/                       # Gerenciamento do Baralho
│   │   └── Deck.kt                 # Criação (52 cartas), embaralhamento Fisher-Yates e draw/discard
│   │
│   ├── evaluator/                  # Motor de Avaliação de Pôquer
│   │   ├── HandType.kt             # As 10 mãos canônicas e valores base
│   │   ├── HandRankingData.kt      # Estrutura determinística para desempates e kickers
│   │   ├── PokerHandResult.kt      # Resultado completo da avaliação
│   │   └── PokerHandEvaluator.kt   // Motor que trata Ás Alto, Ás Baixo (Wheel A-5) e kickers
│   │
│   └── scoring/                    # Motor de Pontuação
│       ├── ScoreModifier.kt        # Interface aberta para bônus futuros (+Chips, +Mult, ×Mult)
│       ├── ScoreResult.kt          # Decomposição dos cálculos para animações na UI
│       └── ScoreCalculator.kt      // Resolução estrita: Score = Total Chips × Total Mult
│
├── ui/                             # 🎨 CAMADA DE APRESENTAÇÃO (Jetpack Compose)
│   ├── theme/                      # Paleta de cores, tipografia e tema escuro de feltro
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── MainActivity.kt             # Laboratório interativo vertical (9:16) com feedback visual
│
└── data/                           # 💾 CAMADA DE DADOS (Room & Persistence - Etapas Futuras)
    ├── local/                      # DAOs, Entidades e Banco de Dados Room
    └── repository/                 # Repositórios unificados para runs e configurações
```

---

## 📐 4. Fórmula Fundamental de Pontuação

A pontuação de cada mão jogada é calculada estritamente através da fórmula:

$$\mathbf{Score} = \mathbf{Total\ Chips} \times \mathbf{Total\ Mult}$$

### Ordem Determinística de Resolução:
1. **Base da Mão:** Determinação dos Chips e Mult base fornecidos pelo `HandType` (escalados pelo nível da mão).
2. **Chips das Cartas:** Soma dos `effectiveChips` de cada carta participante da mão (`scoringCards`). Cartas excedentes (`unusedCards`) não pontuam.
3. **Modificadores de Chips:** Aplicação de bônus aditivos globais ($+\text{Chips}$).
4. **Modificadores de Mult Aditivo:** Aplicação de bônus aditivos globais ($+\text{Mult}$).
5. **Modificadores de Mult Multiplicativo:** Aplicação de fatores multiplicativos ($\times\text{Mult}$).
6. **Multiplicação Final:** $\text{Total Chips} \times \text{Total Mult}$ com tratamento de saturação seguro para evitar overflow de inteiros.

---

## 🧪 5. Testes Automatizados

O projeto conta com suíte de testes unitários em `app/src/test/java/com/example/poker/PokerSystemTest.kt` cobrindo:

* **Baralho e RNG:** Garantia de 52 cartas únicas e determinismo de sementes (mesma `Seed` $\to$ mesma sequência).
* **Todas as 10 Mãos de Pôquer:** Royal Flush, Straight Flush (incluindo *Wheel* A-2-3-4-5), Quadra, Full House, Flush, Sequência, Trinca, Dois Pares, Um Par e Carta Alta.
* **Regras Especiais:** Ás baixo como carta de valor 5 na sequência A-2-3-4-5 e rejeição de sequências inválidas (*wrap-around* K-A-2-3-4).
* **Desempates (*Tie-Breaking*):** Comparação hierárquica precisa entre mãos do mesmo tipo.
* **Cálculo de Pontuação:** Validação da fórmula base e de modificadores compostos (+Chips, +Mult, $\times$Mult).

Para executar os testes via terminal:
```bash
gradle :app:testDebugUnitTest
```

---

## 🚀 6. Como Configurar o Ambiente de Desenvolvimento

### Pré-requisitos
* **Android Studio Ladybug (ou superior)** / IntelliJ IDEA com suporte a Android.
* **JDK 17 ou JDK 21**.
* **Android SDK:** Compile SDK 36, Min SDK 24.

### Instalação e Execução
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/poker-roguelike.git
   cd poker-roguelike
   ```
2. Abra o projeto no Android Studio.
3. Aguarde a sincronização do Gradle (Version Catalog via `gradle/libs.versions.toml`).
4. Selecione um dispositivo ou emulador em modo **Retrato (Portrait 9:16)**.
5. Execute o app (`Shift + F10` ou botão **Run**).

---

## 🗺️ 7. Próximos Passos (Roadmap Incremental)

Seguindo o princípio de progressão incremental e data-driven:

- [x] **Etapa 1:** Fundamentos do Sistema de Pôquer (Baralho, Avaliador, Pontuação e Testes Unitários)
- [ ] **Etapa 2:** Máquina de Estados da Run (`InitializingRun`, `BlindSelection`, `Gameplay`, `Shop`, `GameOver`)
- [ ] **Etapa 3:** Sistema Data-Driven de Coringas e Gatilhos de Eventos (`OnCardPlayed`, `OnScoreCalculated`)
- [ ] **Etapa 4:** Cartas Consumíveis (Tarô, Planetas e Vouchers)
- [ ] **Etapa 5:** Economia, Recompensas de Rodada e Sistema de Loja (*Shop*)
- [ ] **Etapa 6:** Persistência de Run com Room (Salvar estado da partida, histórico e estatísticas)
- [ ] **Etapa 7:** Progressão de Dificuldade, Blinds e Boss Blinds com restrições
- [ ] **Etapa 8:** Tratamento de Números Astronômicos (Aritmética de alta escala / Notação Científica)
- [ ] **Etapa 9:** Polimento de UI, Juiciness, Shaders e Efeitos Sonoros Mobile

---

## 📄 Licença
Distribuído sob licença proprietária para fins educacionais e de desenvolvimento independente.
