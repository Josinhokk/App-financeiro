package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.data.dao.CompromissoMensal;

import java.util.ArrayList;
import java.util.List;

/**
 * Regra 4.3. A partir do compromisso mensal futuro (parcelamentos) ordenado por mês,
 * deriva as liberações: quando o compromisso cai entre mês N-1 e N, o app avisa
 * "a partir do mês N sobram +X por mês".
 */
public final class CronogramaCalculo {

    private CronogramaCalculo() {}

    public static List<Liberacao> derivarLiberacoes(List<CompromissoMensal> mensal) {
        List<Liberacao> resultado = new ArrayList<>();
        for (int i = 1; i < mensal.size(); i++) {
            CompromissoMensal anterior = mensal.get(i - 1);
            CompromissoMensal atual = mensal.get(i);
            long delta = anterior.totalCentavos - atual.totalCentavos;
            if (delta > 0) {
                resultado.add(new Liberacao(atual.mes, delta));
            }
        }
        return resultado;
    }

    public static final class Liberacao {
        public final String mes;
        public final long valorCentavos;

        public Liberacao(String mes, long valorCentavos) {
            this.mes = mes;
            this.valorCentavos = valorCentavos;
        }
    }
}
