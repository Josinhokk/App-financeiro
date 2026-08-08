package com.example.appfinanceiro.ui.comum;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.appfinanceiro.core.MascaraMoeda;

/**
 * Campo de dinheiro que se reescreve a cada tecla: o usuário só digita dígitos
 * e o texto vira "R$ 12,34" sozinho, com o cursor sempre no fim.
 *
 * O valor real vive em {@link #getCentavos()}, nunca no texto — a UI é só a
 * representação.
 */
public class MascaraMoedaWatcher implements TextWatcher {

    private final EditText campo;
    private boolean reescrevendo;
    private long centavos;

    public MascaraMoedaWatcher(EditText campo) {
        this.campo = campo;
    }

    public long getCentavos() {
        return centavos;
    }

    /** Preenche o campo programaticamente, por exemplo ao editar um balde. */
    public void setCentavos(long valor) {
        centavos = valor;
        reescrevendo = true;
        String texto = MascaraMoeda.formatarCentavos(valor);
        campo.setText(texto);
        campo.setSelection(texto.length());
        reescrevendo = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        // setText() abaixo dispara este callback de novo; sem a guarda, recursão.
        if (reescrevendo) return;

        reescrevendo = true;
        centavos = MascaraMoeda.extrairCentavos(s.toString());
        String texto = MascaraMoeda.formatarCentavos(centavos);
        campo.setText(texto);
        campo.setSelection(texto.length());
        reescrevendo = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
