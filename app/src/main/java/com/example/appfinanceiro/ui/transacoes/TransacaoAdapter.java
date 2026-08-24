package com.example.appfinanceiro.ui.transacoes;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfinanceiro.R;
import com.example.appfinanceiro.core.Dinheiro;
import com.example.appfinanceiro.data.dao.TransacaoComBalde;
import com.example.appfinanceiro.data.entity.TipoTransacao;
import com.example.appfinanceiro.domain.ItemLista;
import com.google.android.material.color.MaterialColors;

public class TransacaoAdapter extends ListAdapter<ItemLista, RecyclerView.ViewHolder> {

    private static final int TIPO_CABECALHO = 0;
    private static final int TIPO_LANCAMENTO = 1;

    public TransacaoAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<ItemLista> DIFF = new DiffUtil.ItemCallback<ItemLista>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemLista a, @NonNull ItemLista b) {
            if (a instanceof ItemLista.Cabecalho && b instanceof ItemLista.Cabecalho) {
                return ((ItemLista.Cabecalho) a).data.equals(((ItemLista.Cabecalho) b).data);
            }
            if (a instanceof ItemLista.Lancamento && b instanceof ItemLista.Lancamento) {
                return ((ItemLista.Lancamento) a).item.transacao.getId()
                        == ((ItemLista.Lancamento) b).item.transacao.getId();
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ItemLista a, @NonNull ItemLista b) {
            if (a instanceof ItemLista.Cabecalho && b instanceof ItemLista.Cabecalho) {
                return ((ItemLista.Cabecalho) a).totalCentavos == ((ItemLista.Cabecalho) b).totalCentavos;
            }
            if (a instanceof ItemLista.Lancamento && b instanceof ItemLista.Lancamento) {
                TransacaoComBalde x = ((ItemLista.Lancamento) a).item;
                TransacaoComBalde y = ((ItemLista.Lancamento) b).item;
                return x.transacao.getValorCentavos() == y.transacao.getValorCentavos()
                        && java.util.Objects.equals(x.transacao.getDescricao(), y.transacao.getDescricao())
                        && java.util.Objects.equals(x.baldeNome, y.baldeNome);
            }
            return false;
        }
    };

    @Override
    public int getItemViewType(int posicao) {
        return getItem(posicao) instanceof ItemLista.Cabecalho ? TIPO_CABECALHO : TIPO_LANCAMENTO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup pai, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(pai.getContext());
        if (viewType == TIPO_CABECALHO) {
            return new CabecalhoVH(inflater.inflate(R.layout.item_cabecalho_data, pai, false));
        }
        return new LancamentoVH(inflater.inflate(R.layout.item_transacao, pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int posicao) {
        ItemLista item = getItem(posicao);
        if (vh instanceof CabecalhoVH) {
            ((CabecalhoVH) vh).ligar((ItemLista.Cabecalho) item);
        } else {
            ((LancamentoVH) vh).ligar((ItemLista.Lancamento) item);
        }
    }

    static class CabecalhoVH extends RecyclerView.ViewHolder {
        private final TextView rotulo;
        private final TextView total;

        CabecalhoVH(@NonNull View v) {
            super(v);
            rotulo = v.findViewById(R.id.rotulo_data);
            total = v.findViewById(R.id.total_dia);
        }

        void ligar(ItemLista.Cabecalho c) {
            rotulo.setText(c.rotulo);
            total.setText(Dinheiro.formatarCentavos(c.totalCentavos));
        }
    }

    static class LancamentoVH extends RecyclerView.ViewHolder {
        private final View pontoCor;
        private final TextView descricao;
        private final TextView balde;
        private final TextView valor;

        LancamentoVH(@NonNull View v) {
            super(v);
            pontoCor = v.findViewById(R.id.ponto_cor);
            descricao = v.findViewById(R.id.descricao_transacao);
            balde = v.findViewById(R.id.balde_transacao);
            valor = v.findViewById(R.id.valor_transacao);
        }

        void ligar(ItemLista.Lancamento l) {
            TransacaoComBalde t = l.item;

            String texto = t.transacao.getDescricao();
            boolean semDescricao = texto == null || texto.trim().isEmpty();
            descricao.setText(semDescricao
                    ? itemView.getContext().getString(R.string.sem_descricao)
                    : texto);

            balde.setText(t.baldeNome != null
                    ? t.baldeNome
                    : itemView.getContext().getString(R.string.sem_balde));

            // Sinal explícito: a seção 9 proíbe depender só de cor.
            boolean saida = t.transacao.getTipo() == TipoTransacao.SAIDA;
            String sinal = saida ? "−" : "+";
            valor.setText(sinal + " " + Dinheiro.formatarCentavos(t.transacao.getValorCentavos()));
            // Saída lê do tema para acompanhar o modo escuro; entrada é sempre o verde positivo.
            valor.setTextColor(saida
                    ? MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface)
                    : itemView.getContext().getColor(R.color.positivo));

            pontoCor.setBackgroundColor(corDe(t.baldeCorHex));
        }

        private int corDe(String hex) {
            if (hex == null || hex.isEmpty()) return Color.parseColor("#6B7280");
            try {
                return Color.parseColor(hex);
            } catch (IllegalArgumentException e) {
                return Color.parseColor("#6B7280");
            }
        }
    }
}
