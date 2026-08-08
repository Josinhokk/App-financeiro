# App de Finanças Pessoais

Plano completo: `docs/plano-app-financas-v2.md`. Leia antes de qualquer tarefa.

## Stack
Java 17, Android minSdk 26, XML + Material 3, Room, MVVM, Navigation Component.

## Regras inegociáveis
- Dinheiro é `long` de centavos no banco e `BigDecimal` em memória. NUNCA `double`.
- Datas com `java.time.LocalDate` via TypeConverter.
- Toda regra de cálculo tem teste unitário antes da UI.
- `dataCompetencia` (consome balde) e `dataCaixa` (sai da conta) são campos distintos.
- Parcelamento distribui competência: uma parcela por mês, nunca o total de uma vez.

## Fluxo de trabalho
- Uma fase por vez. Não avance sem eu aprovar.
- Rode `./gradlew testDebugUnitTest` antes de dizer que terminou.
- Commit ao fim de cada fase.