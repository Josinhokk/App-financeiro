package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.data.dao.TransacaoComBalde;

import java.time.LocalDate;

/**
 * Uma linha da lista de transações. A lista é plana — cabeçalhos de dia e
 * lançamentos convivem na mesma sequência, que é o que o RecyclerView espera.
 */
public abstract class ItemLista {

    private ItemLista() {}

    /** Separador de dia, com o total gasto naquele dia. */
    public static final class Cabecalho extends ItemLista {
        public final LocalDate data;
        public final String rotulo;
        public final long totalCentavos;

        public Cabecalho(LocalDate data, String rotulo, long totalCentavos) {
            this.data = data;
            this.rotulo = rotulo;
            this.totalCentavos = totalCentavos;
        }
    }

    public static final class Lancamento extends ItemLista {
        public final TransacaoComBalde item;

        public Lancamento(TransacaoComBalde item) {
            this.item = item;
        }
    }
}
