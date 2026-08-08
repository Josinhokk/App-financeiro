package com.example.appfinanceiro.data.db;

import com.example.appfinanceiro.data.dao.BaldeDao;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.entity.TipoBalde;

/** Baldes iniciais da seção 8 do plano. Executado uma vez, na criação do banco. */
public final class Seed {

    private Seed() {}

    public static void popularBaldes(BaldeDao dao) {
        dao.inserir(balde("Fixos", TipoBalde.FIXO, 73000, false, 0, "#6B7280"));
        dao.inserir(balde("Investimento", TipoBalde.INVESTIMENTO, 240000, true, 1, "#10B981"));
        dao.inserir(balde("Lazer", TipoBalde.LIVRE, 80000, false, 2, "#34D399"));
        dao.inserir(balde("Switch 2", TipoBalde.META, 27000, true, 3, "#F59E0B"));
    }

    private static Balde balde(String nome, TipoBalde tipo, long mensalCents, boolean rola, int ordem, String cor) {
        Balde b = new Balde();
        b.setNome(nome);
        b.setTipo(tipo);
        b.setValorMensalCentavos(mensalCents);
        b.setRolaSaldo(rola);
        b.setOrdem(ordem);
        b.setCorHex(cor);
        return b;
    }
}
