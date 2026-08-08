# Plano de Desenvolvimento — App de Finanças Pessoais (v2)

> Documento de planejamento revisado. Plataforma: Android nativo, uso pessoal (sideload).
> Destino: servir de briefing para implementação assistida (Claude Code).

---

## 0. O que mudou da v1 para a v2

A v1 era um app de **registro**: respondia "para onde foi meu dinheiro?". A v2 é um app de **decisão**: responde "quanto ainda posso gastar sem quebrar o plano?".

Mudanças estruturais:

1. **Nova entidade central: `Balde`.** Orçamento por categoria diz *onde* o dinheiro foi. Balde diz *de qual bolso* ele saiu. São eixos diferentes e o app precisa dos dois.
2. **Nova entidade: `Parcelamento`.** Parcelas têm data de término conhecida. O app usa isso para projetar quando a capacidade de aporte aumenta.
3. **Regime de competência.** Gasto no cartão conta no balde na data da compra, não no vencimento da fatura. Isso separa "impacto no orçamento" de "saída de caixa".
4. **Fases reordenadas.** Transações e baldes nascem juntos. Gráficos foram adiados. Dívidas a receber saiu do escopo inicial.
5. **Dashboard com outro herói.** Saldo em conta deu lugar a "disponível no balde livre".

---

## 1. Princípio norteador

**O app existe para responder uma pergunta, numa tela, em menos de dois segundos:**

> Quanto eu ainda posso gastar este mês?

Toda decisão de design se subordina a isso. Se uma feature não ajuda a responder essa pergunta ou a manter a resposta confiável, ela é secundária por definição.

Corolário importante: **o custo de registrar um gasto é o maior risco do projeto.** O modo de lançamento escolhido é manual, gasto a gasto. Isso dá dados perfeitos e é também o motivo pelo qual 90% dos apps de finanças pessoais são abandonados em seis semanas. O atrito de lançamento é requisito de primeira classe, não polimento. Ver seção 7.

---

## 2. Decisões de arquitetura (ADRs)

Registradas explicitamente porque são caras de reverter depois que houver dados em produção.

### ADR-01 — Stack

| Camada | Decisão |
|---|---|
| Linguagem | Java |
| UI | XML + Material Components 3 |
| Banco | Room (SQLite) |
| Arquitetura | MVVM (ViewModel + Repository) |
| Navegação | Navigation Component + Bottom Navigation |
| Agendamento | WorkManager (recorrências, notificações) |

Mantida da v1. Kotlin/Compose ficam como evolução futura, não como reescrita.

### ADR-02 — Dinheiro é `BigDecimal`, nunca `double`

Ponto flutuante binário não representa 0,10 exatamente. Somar centenas de transações com `double` produz divergências de centavos que aparecem justamente na conciliação com a fatura — o momento em que a confiança no app é decidida.

Armazenar no Room como `long` de centavos (via `TypeConverter`) e trabalhar em memória com `BigDecimal`. Todas as divisões usam `RoundingMode.HALF_UP` com escala 2.

### ADR-03 — Regime de competência para o balde, regime de caixa para a conta

Esta é a decisão mais sutil do documento.

Uma compra de R$ 200 no cartão dia 20 de agosto, com fatura vencendo em 10 de setembro, gera **dois eventos distintos**:

- **Competência:** consome R$ 200 do balde em agosto. É quando a decisão de gastar foi tomada.
- **Caixa:** R$ 200 saem da conta corrente em setembro, junto com o resto da fatura.

O app modela os dois. O balde usa competência (é sobre disciplina de decisão). O saldo da conta usa caixa (é sobre não ficar no vermelho).

**Regra para parcelamentos:** uma compra parcelada em 12x **não** consome o valor total do balde no mês da compra. Cada parcela consome o balde no mês em que incide. Caso contrário, um único parcelamento destruiria o orçamento de um mês e deixaria os onze seguintes artificialmente folgados — o oposto do que a realidade financeira faz.

Ou seja: **à vista → competência na data da compra. Parcelado → competência distribuída, uma parcela por mês.**

### ADR-04 — Baldes coexistem com Categorias, não os substituem

- **Categoria** = taxonomia do gasto (Mercado, Suplemento, Combustível). Serve para relatório.
- **Balde** = origem orçamentária (Fixos, Lazer, Meta Switch). Serve para decisão.

Um gasto tem exatamente um de cada. Um whey e um jogo têm categorias diferentes; podem ou não estar no mesmo balde, e é o balde que determina se o gasto é permitido.

### ADR-05 — Offline-first, sem backend

Nenhuma conta, nenhum login, nenhuma sincronização. Backup é exportação manual para CSV/JSON. Isso elimina a maior fonte de complexidade do projeto e não custa nada em funcionalidade para uso pessoal em um único aparelho.

---

## 3. Modelo de dados

### 3.1 Entidades

```
Conta            — carteira física ou lógica (corrente, dinheiro, cartão)
Categoria        — taxonomia do gasto
Balde            — bolso orçamentário mensal          [NOVO]
Transacao        — evento de competência (consome balde)
Parcelamento     — compra dividida em N parcelas      [NOVO]
Recorrencia      — despesa fixa que se repete
Fatura           — agrupamento de caixa do cartão     [NOVO]
Aporte           — investimento realizado
Meta             — objetivo com valor-alvo
```

### 3.2 Balde

```java
@Entity(tableName = "baldes")
public class Balde {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome;                 // "Lazer", "Switch 2"
    private TipoBalde tipo;              // FIXO, INVESTIMENTO, META, LIVRE
    private long valorMensalCentavos;    // dotação mensal
    private boolean rolaSaldo;           // acumula ou zera no dia 1?
    private long saldoAcumuladoCentavos; // herança de meses anteriores
    private String corHex;
    private String icone;
    private int ordem;
    private boolean arquivado;
}
```

O campo `rolaSaldo` é o coração do modelo:

| Balde | `rolaSaldo` | Comportamento |
|---|---|---|
| Lazer | `false` | Dia 1, volta a R$ 800 limpos. Não carrega culpa nem crédito |
| Switch 2 | `true` | Cada mês empilha até virar console |
| Fixos | `false` | Reseta; o que sobra vira folga do mês, não bônus futuro |
| Investimento | `true` | Acumula até o aporte ser efetivado |

Zerar o balde de lazer todo mês é decisão de comportamento, não limitação técnica. Saldo negativo que se arrasta transforma o app num cobrador, e cobrador se desinstala.

### 3.3 Transacao

```java
@Entity(tableName = "transacoes",
        foreignKeys = { /* Conta, Categoria, Balde, Parcelamento */ },
        indices = { @Index("dataCompetencia"), @Index("baldeId") })
public class Transacao {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long valorCentavos;
    private TipoTransacao tipo;          // ENTRADA, SAIDA, TRANSFERENCIA
    private LocalDate dataCompetencia;   // consome o balde nesta data
    private LocalDate dataCaixa;         // sai da conta nesta data (nullable)
    private String descricao;

    private long contaId;
    private Long categoriaId;            // nullable
    private Long baldeId;                // nullable (entradas não têm balde)
    private Long parcelamentoId;         // preenchido se veio de parcelamento
    private Integer numeroParcela;       // 2 de 12
    private Long recorrenciaId;          // preenchido se gerado automaticamente
}
```

Para compra à vista no débito, `dataCompetencia == dataCaixa`. Para cartão, `dataCaixa` é o vencimento da fatura correspondente.

### 3.4 Parcelamento

```java
@Entity(tableName = "parcelamentos")
public class Parcelamento {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String descricao;            // "Shopee"
    private long valorParcelaCentavos;   // 17393
    private int totalParcelas;           // 12
    private int parcelaInicial;          // 2 (para cadastrar em andamento)
    private LocalDate mesPrimeiraParcela;
    private long contaId;                // qual cartão
    private Long baldeId;
    private boolean quitado;
}
```

Ao criar um `Parcelamento`, o app **gera as N `Transacao` futuras de uma vez**, uma por mês de competência. Isso torna a projeção trivial: a query de "quanto vou ter de parcela em março de 2027" é uma soma comum, não um cálculo especial.

Suporte a **cadastro em andamento** (`parcelaInicial > 1`) é obrigatório: as parcelas atuais já estão na parcela 2 de 12 e 10 de 12.

### 3.5 Fatura

```java
@Entity(tableName = "faturas")
public class Fatura {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long contaId;                // cartão
    private int mesReferencia;           // 202609
    private LocalDate fechamento;
    private LocalDate vencimento;
    private long valorPagoCentavos;      // preenchido ao conciliar
    private StatusFatura status;         // ABERTA, FECHADA, PAGA
}
```

A fatura existe para **conciliação**: no dia do pagamento, você compara o total calculado pelo app com o valor real do app do banco. Divergência acusa lançamento esquecido. Esse é o mecanismo de autocorreção do sistema.

### 3.6 Demais entidades

```java
Recorrencia { descricao, valorCentavos, frequencia, diaDoMes,
              baldeId, categoriaId, contaId, ativa, proximaGeracao }

Aporte      { ativo, valorCentavos, data, classe /* RF, RV, RESERVA */ }

Meta        { nome, tipo /* RESERVA, COMPRA, INDEPENDENCIA */,
              valorAlvoCentavos, baldeId, dataAlvo }
```

**Fora do escopo inicial:** `DividaReceber` e `Abatimento`. É a modelagem mais divertida do plano original e resolve um problema que hoje não existe. Volta na v3, se voltar.

---

## 4. Regras de cálculo

### 4.1 Disponível no balde (a query mais importante do app)

```java
// DAO
@Query("SELECT COALESCE(SUM(valorCentavos), 0) FROM transacoes " +
       "WHERE baldeId = :baldeId " +
       "AND tipo = 'SAIDA' " +
       "AND dataCompetencia BETWEEN :inicio AND :fim")
long somaGastosDoPeriodo(long baldeId, LocalDate inicio, LocalDate fim);
```

```java
// Repository
public BigDecimal disponivelNoMes(Balde balde, YearMonth mes) {
    long gasto = dao.somaGastosDoPeriodo(
        balde.getId(), mes.atDay(1), mes.atEndOfMonth());

    long disponivel = balde.getValorMensalCentavos() - gasto;

    if (balde.isRolaSaldo()) {
        disponivel += balde.getSaldoAcumuladoCentavos();
    }
    return Dinheiro.deCentavos(disponivel);
}
```

### 4.2 Virada de mês

Executada por `WorkManager` no dia 1, e também de forma preguiçosa na abertura do app (defesa contra o Doze mode do Android matar o job):

```
para cada balde:
    se rolaSaldo:
        saldoAcumulado = disponivelNoMes(balde, mesAnterior)
    senão:
        saldoAcumulado = 0
```

A virada precisa ser **idempotente**. Marque o último mês processado numa tabela de controle e não reprocesse. Rodar duas vezes um `rolaSaldo` dobra o patrimônio virtual do usuário, o que é engraçado exatamente uma vez.

### 4.3 Cronograma de liberação

A projeção que transforma parcela chata em contagem regressiva:

```java
@Query("SELECT strftime('%Y-%m', dataCompetencia) AS mes, " +
       "       SUM(valorCentavos) AS total " +
       "FROM transacoes " +
       "WHERE parcelamentoId IS NOT NULL " +
       "AND dataCompetencia >= :hoje " +
       "GROUP BY mes ORDER BY mes")
List<CompromissoMensal> compromissoFuturoPorMes(LocalDate hoje);
```

Comparando meses consecutivos, o app deriva sozinho: *"a partir de julho/2027 sobram mais R$ 173,93 por mês"*. Essa é a tela que mantém a disciplina, porque dá data para o alívio.

### 4.4 Validação de integridade

Regras que o app checa e sinaliza sem bloquear:

- Soma das dotações dos baldes ≤ renda mensal cadastrada.
- Total calculado da fatura vs. valor real informado na conciliação.
- Transação com `dataCaixa` anterior à `dataCompetencia` (provável erro de digitação).

---

## 5. Fases de desenvolvimento

Cada fase entrega algo usável. A regra é: nenhuma fase termina sem que o app esteja melhor de usar do que estava antes.

### Fase 0 — Fundação
- Projeto Android, estrutura MVVM, Room configurado.
- `TypeConverter` para `LocalDate` e para dinheiro em centavos.
- Classe utilitária `Dinheiro` (fábrica, formatação pt-BR, aritmética segura).
- Bottom Navigation com as abas vazias.
- **Seed inicial:** baldes e categorias padrão já populados (ver seção 8).

### Fase 1 — Núcleo: transações + baldes
Entregues **juntos**. Transação sem balde é diário sem consequência.
- CRUD de transação com seleção de balde.
- CRUD de balde.
- Cálculo de disponível.
- Tela de adicionar gasto otimizada para velocidade (seção 7).
- **Marco:** o app já responde a pergunta do princípio norteador.

### Fase 2 — Dashboard
- Herói: disponível no balde livre.
- Cards secundários: status dos fixos, aporte do mês, progresso das metas.
- Estados vazios acolhedores.

### Fase 3 — Parcelamentos e cronograma
Alto valor, baixo esforço — é aritmética de datas sobre dados que já existem.
- Cadastro de parcelamento com geração das transações futuras.
- Suporte a parcelamento já em andamento.
- Tela de cronograma de liberação.
- Alerta ao criar parcelamento que estoure o teto definido.

### Fase 4 — Recorrências e fatura
- Recorrências geradas automaticamente pelo `WorkManager`.
- Agrupamento de fatura e tela de conciliação.
- Notificação de vencimento.

### Fase 5 — Virada de mês e widget
- Job de virada idempotente.
- Widget de home screen com o disponível (seção 7.3).
- Notificação opcional de resumo semanal.

### Fase 6 — Relatórios
- Gasto por categoria (rosca).
- Evolução do disponível ao longo do mês (linha) — mais útil que evolução de saldo.
- Filtros por período.

### Fase 7 — Investimentos e patrimônio
- Registro de aportes e composição.
- Evolução do patrimônio.
- Metas de reserva e independência financeira com projeção de juros compostos.

### Fase 8 — Polimento
- Modo escuro.
- Exportação CSV/JSON e importação (backup real, não decorativo).
- Testes unitários das regras de cálculo — prioridade para virada de mês e geração de parcelas.

### Backlog (não priorizado)
- Dívidas a receber com log de abatimentos.
- Importação de OFX/CSV de fatura.
- Open Finance via agregador (estudo de portfólio).

---

## 6. Dashboard

O herói muda de saldo para disponível. Saldo em conta é enganoso: inclui dinheiro que já tem dono.

```
┌─────────────────────────────┐
│                             │
│      LIVRE ESTE MÊS         │
│         R$ 612              │
│   ▓▓▓▓▓▓▓▓▓░░░  de R$ 800   │
│      restam 9 dias          │
│                             │
├─────────────────────────────┤
│ Fixos      ✓ R$ 730 pagos   │
│ Investido  ✓ R$ 2.400       │
├─────────────────────────────┤
│ Switch 2                    │
│ ▓▓▓░░░░░  R$ 810 / 2.400    │
│ previsão: março/2027        │
├─────────────────────────────┤
│ Próxima liberação           │
│ dez/2026  +R$ 70,85/mês     │
└─────────────────────────────┘
```

Detalhe que importa: **"restam 9 dias"** ao lado do valor. R$ 612 com 9 dias pela frente é uma informação; R$ 612 com 25 dias pela frente é outra completamente diferente. Um número de ritmo (`disponível ÷ dias restantes`) vale mais que um número absoluto.

---

## 7. Atrito de lançamento

O modo escolhido é manual, gasto a gasto. Isso é o que produz dados confiáveis e é também o ponto único de falha do projeto. As contramedidas abaixo não são polimento; são requisito.

### 7.1 Meta de três toques

Do FAB até o gasto salvo:

1. **Toque no FAB** → abre já com o teclado numérico em foco e a data em hoje.
2. **Digita o valor** → chips de balde aparecem abaixo, ordenados por frequência de uso.
3. **Toque no balde** → salva e fecha.

Categoria, descrição e conta são **opcionais** e ficam atrás de um "mais detalhes". Um gasto lançado sem categoria é infinitamente melhor que um gasto não lançado.

### 7.2 Aceleradores

- **Sugestões de valor:** chips com os 3 valores mais frequentes do balde selecionado.
- **Repetir último:** long-press no FAB relança o gasto mais recente.
- **Modelos:** gastos frequentes (almoço, combustível) viram atalho de um toque.
- **Recorrências automáticas:** academia, internet e parcelas nunca são digitadas. Se o usuário precisa lançar manualmente algo que acontece todo mês, o app falhou.

### 7.3 Widget de home screen

Um widget pequeno mostrando o disponível do balde livre, com toque abrindo direto o formulário de lançamento. O widget serve a dois propósitos ao mesmo tempo: elimina o passo de abrir o app e mantém o número visível sem exigir nada do usuário.

### 7.4 Sem gamificação punitiva

Nada de streaks, badges ou notificação de cobrança por dias sem lançamento. Isso funciona para app de idioma e fracassa para dinheiro, onde a emoção de base já é ansiedade. O reforço vem do número da meta subindo, não de um app repreendendo o usuário.

---

## 8. Dados iniciais (seed)

Para o app ser útil na primeira abertura, sem tela de configuração longa.

### Baldes

| Nome | Tipo | Mensal | Rola |
|---|---|---|---|
| Fixos | FIXO | 730,00 | não |
| Investimento | INVESTIMENTO | 2.400,00 | sim |
| Lazer | LIVRE | 800,00 | não |
| Switch 2 | META | 270,00 | sim |

### Recorrências

| Descrição | Valor | Dia | Balde |
|---|---|---|---|
| Academia | 120,00 | 5 | Fixos |
| Internet | 65,00 | 10 | Fixos |
| Combustível | 50,00 | 15 | Fixos |
| Whey | 180,00 | bimestral | Fixos |

### Parcelamentos em andamento

| Descrição | Parcela | Atual | Total |
|---|---|---|---|
| Shopee | 173,93 | 2 | 12 |
| App Elements | 152,11 | 2 | 10 |
| Alura | 70,85 | 10 | 12 |

### Categorias

Saída: Mercado, Restaurante, Combustível, Suplemento, Assinatura, Educação, Saúde, Lazer, Eletrônicos, Outros.
Entrada: Salário, 13º, Reembolso, Outros.

### Metas

| Nome | Tipo | Alvo | Atual |
|---|---|---|---|
| Reserva de emergência | RESERVA | 5.000,00 | 5.500,00 (batida) |
| Nintendo Switch 2 | COMPRA | 2.400,00 | 0 |

---

## 9. Identidade visual

Mantida da v1.

| Uso | Hex |
|---|---|
| Primária (verde-esmeralda) | `#10B981` |
| Primária escura | `#047857` |
| Positivo | `#34D399` |
| Negativo | `#EF4444` |
| Alerta (balde perto do limite) | `#F59E0B` |
| Fundo claro | `#F9FAFB` |
| Fundo escuro | `#111827` |
| Superfície | `#FFFFFF` / `#1F2937` |
| Texto principal | `#1F2937` |
| Texto secundário | `#6B7280` |

Adição: a **barra de progresso do balde** usa três estados — verde até 70%, âmbar de 70% a 100%, vermelho acima. É o principal canal de feedback do app e precisa ser lido em meio segundo.

Tipografia: Roboto ou Inter. Valores monetários com peso forte e corpo grande; rótulos pequenos em cinza. Cards com cantos de 12–16dp e espaçamento generoso.

Acessibilidade: contraste AA, alvos de toque de 48dp, nunca depender só de cor (sempre acompanhar de ícone ou sinal +/−).

---

## 10. Riscos

| Risco | Probabilidade | Mitigação |
|---|---|---|
| Abandono por atrito de lançamento | **Alta** | Seção 7 inteira; recorrências automáticas; widget |
| Bug na virada de mês corrompendo saldos | Média | Idempotência, tabela de controle, testes unitários |
| Escopo crescendo antes da Fase 3 | Média | Backlog explícito; dívidas a receber já isoladas |
| Divergência com a fatura real | Média | Tela de conciliação mensal |
| Perda de dados (celular novo, wipe) | Baixa | Exportação na Fase 8; considerar antecipar se o app pegar |

O primeiro risco é o único que mata o projeto. Os outros são incômodos.

---

## 11. Ordem de ataque sugerida

Para implementação assistida, esta é a sequência que mantém o app funcional a cada passo:

1. `Dinheiro`, `TypeConverter`s, entidades `Balde` e `Transacao`, DAOs, testes das regras de cálculo.
2. Tela de lançamento rápido (o caminho de três toques) e lista de transações.
3. Dashboard com o card de disponível.
4. `Parcelamento` com geração de transações futuras + tela de cronograma.
5. `Recorrencia` + `WorkManager`.
6. Virada de mês idempotente.
7. Widget.
8. O resto conforme as fases.

Comece pelos testes das regras de cálculo antes da UI. São poucas regras, elas são a única parte do app onde um erro silencioso destrói a confiança no produto inteiro, e são triviais de testar porque não dependem de Android.
