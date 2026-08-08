package com.example.appfinanceiro.ui.baldes;

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
import com.example.appfinanceiro.data.entity.Balde;

public class BaldeAdapter extends ListAdapter<Balde, BaldeAdapter.ItemVH> {

    public interface AoTocar {
        void no(Balde balde);
    }

    private final AoTocar aoTocar;

    public BaldeAdapter(AoTocar aoTocar) {
        super(DIFF);
        this.aoTocar = aoTocar;
    }

    private static final DiffUtil.ItemCallback<Balde> DIFF = new DiffUtil.ItemCallback<Balde>() {
        @Override
        public boolean areItemsTheSame(@NonNull Balde a, @NonNull Balde b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Balde a, @NonNull Balde b) {
            return a.getNome().equals(b.getNome())
                    && a.getTipo() == b.getTipo()
                    && a.getValorMensalCentavos() == b.getValorMensalCentavos()
                    && a.isRolaSaldo() == b.isRolaSaldo()
                    && a.getOrdem() == b.getOrdem();
        }
    };

    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup pai, int viewType) {
        View v = LayoutInflater.from(pai.getContext())
                .inflate(R.layout.item_balde, pai, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVH vh, int posicao) {
        vh.ligar(getItem(posicao), aoTocar);
    }

    static class ItemVH extends RecyclerView.ViewHolder {

        private final View faixaCor;
        private final TextView nome;
        private final TextView subtitulo;
        private final TextView valor;

        ItemVH(@NonNull View v) {
            super(v);
            faixaCor = v.findViewById(R.id.faixa_cor);
            nome = v.findViewById(R.id.nome_balde);
            subtitulo = v.findViewById(R.id.subtitulo_balde);
            valor = v.findViewById(R.id.valor_balde);
        }

        void ligar(Balde b, AoTocar aoTocar) {
            nome.setText(b.getNome());
            valor.setText(Dinheiro.formatarCentavos(b.getValorMensalCentavos()));

            String tipo = itemView.getContext().getString(TipoBaldeUi.rotulo(b.getTipo()));
            int rolaResId = b.isRolaSaldo() ? R.string.rola_saldo_curto : R.string.zera_curto;
            subtitulo.setText(itemView.getContext()
                    .getString(R.string.subtitulo_balde, tipo, itemView.getContext().getString(rolaResId)));

            faixaCor.setBackgroundColor(corDe(b));
            itemView.setOnClickListener(v -> aoTocar.no(b));
        }

        /** Cor gravada no balde; hex inválido ou ausente cai no padrão do tipo. */
        private int corDe(Balde b) {
            String hex = b.getCorHex();
            if (hex == null || hex.isEmpty()) {
                hex = TipoBaldeUi.corPadrao(b.getTipo());
            }
            try {
                return Color.parseColor(hex);
            } catch (IllegalArgumentException e) {
                return Color.parseColor(TipoBaldeUi.corPadrao(b.getTipo()));
            }
        }
    }
}
