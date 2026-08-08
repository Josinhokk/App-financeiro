package com.example.appfinanceiro.ui.baldes;

import androidx.annotation.StringRes;

import com.example.appfinanceiro.R;
import com.example.appfinanceiro.data.entity.TipoBalde;

/**
 * Apresentação do enum. Um switch explícito em vez de indexar por ordinal:
 * reordenar o enum não pode silenciosamente trocar os rótulos.
 */
public final class TipoBaldeUi {

    private TipoBaldeUi() {}

    @StringRes
    public static int rotulo(TipoBalde tipo) {
        switch (tipo) {
            case FIXO: return R.string.tipo_fixo;
            case INVESTIMENTO: return R.string.tipo_investimento;
            case META: return R.string.tipo_meta;
            case LIVRE: return R.string.tipo_livre;
        }
        throw new IllegalArgumentException("TipoBalde sem rótulo: " + tipo);
    }

    /** Cor padrão de um balde novo, alinhada com as do seed (seção 8). */
    public static String corPadrao(TipoBalde tipo) {
        switch (tipo) {
            case FIXO: return "#6B7280";
            case INVESTIMENTO: return "#10B981";
            case META: return "#F59E0B";
            case LIVRE: return "#34D399";
        }
        return "#10B981";
    }
}
