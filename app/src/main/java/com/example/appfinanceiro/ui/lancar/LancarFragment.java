package com.example.appfinanceiro.ui.lancar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appfinanceiro.R;
import com.example.appfinanceiro.core.Dinheiro;
import com.example.appfinanceiro.core.DatasPtBr;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.domain.ValidacaoTransacao;
import com.example.appfinanceiro.ui.baldes.TipoBaldeUi;
import com.example.appfinanceiro.ui.comum.MascaraMoedaWatcher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Caminho de três toques da seção 7.1: abrir a aba, digitar o valor, tocar no
 * balde. Categoria, descrição e data ficam atrás de "mais detalhes" — um gasto
 * lançado sem categoria é infinitamente melhor que um gasto não lançado.
 */
public class LancarFragment extends Fragment {

    private LancarViewModel vm;
    private MascaraMoedaWatcher mascara;

    private EditText valor;
    private MaterialButton botaoData;
    private View blocoBaldes;
    private ChipGroup chips;
    private TextInputLayout descricaoLayout;
    private TextInputEditText descricao;

    private LocalDate data = LocalDate.now();

    public LancarFragment() {
        super(R.layout.fragment_lancar);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);

        vm = new ViewModelProvider(this).get(LancarViewModel.class);

        valor = v.findViewById(R.id.campo_valor);
        botaoData = v.findViewById(R.id.botao_data);
        blocoBaldes = v.findViewById(R.id.bloco_baldes);
        chips = v.findViewById(R.id.chips_baldes);
        descricaoLayout = v.findViewById(R.id.campo_descricao_layout);
        descricao = v.findViewById(R.id.campo_descricao);

        mascara = new MascaraMoedaWatcher(valor);
        valor.addTextChangedListener(mascara);
        mascara.setAoMudarValor(this::aoMudarValor);
        mascara.setCentavos(0);

        atualizarRotuloDaData();
        botaoData.setOnClickListener(x -> escolherData());

        MaterialButton maisDetalhes = v.findViewById(R.id.botao_mais_detalhes);
        maisDetalhes.setOnClickListener(x -> alternarDetalhes(maisDetalhes));

        vm.baldes().observe(getViewLifecycleOwner(), this::montarChips);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Teclado numérico já em foco: o passo 1 não deve custar um toque extra.
        valor.requestFocus();
        valor.post(() -> {
            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(valor, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void aoMudarValor(long centavos) {
        blocoBaldes.setVisibility(centavos > 0 ? View.VISIBLE : View.GONE);
    }

    private void montarChips(@Nullable List<Balde> baldes) {
        chips.removeAllViews();
        if (baldes == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Balde b : baldes) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_balde, chips, false);
            chip.setText(b.getNome());
            chip.setChipStrokeColor(ColorStateList.valueOf(corDe(b)));
            chip.setOnClickListener(x -> lancarNo(b));
            chips.addView(chip);
        }
    }

    private int corDe(Balde b) {
        String hex = b.getCorHex();
        if (hex == null || hex.isEmpty()) hex = TipoBaldeUi.corPadrao(b.getTipo());
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return Color.parseColor(TipoBaldeUi.corPadrao(b.getTipo()));
        }
    }

    /** Terceiro toque: escolher o balde é o mesmo gesto que salvar. */
    private void lancarNo(Balde balde) {
        long centavos = mascara.getCentavos();

        if (ValidacaoTransacao.validar(centavos, balde.getId()) != ValidacaoTransacao.Erro.NENHUM) {
            return; // O bloco de baldes só existe com valor > 0; aqui seria bug.
        }

        String texto = descricao.getText() == null ? null : descricao.getText().toString();
        vm.lancar(centavos, balde.getId(), data, texto);

        Snackbar.make(requireView(),
                getString(R.string.lancado_em,
                        Dinheiro.formatarCentavos(centavos), balde.getNome()),
                Snackbar.LENGTH_SHORT).show();

        limpar();
    }

    private void limpar() {
        mascara.setCentavos(0);
        descricao.setText("");
        data = LocalDate.now();
        atualizarRotuloDaData();
        valor.requestFocus();
    }

    private void alternarDetalhes(MaterialButton botao) {
        boolean mostrando = descricaoLayout.getVisibility() == View.VISIBLE;
        descricaoLayout.setVisibility(mostrando ? View.GONE : View.VISIBLE);
        botao.setText(mostrando ? R.string.mais_detalhes : R.string.menos_detalhes);
        if (!mostrando) descricao.requestFocus();
    }

    private void atualizarRotuloDaData() {
        botaoData.setText(DatasPtBr.rotuloRelativo(data, LocalDate.now()));
    }

    private void escolherData() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.data_da_compra)
                .setSelection(data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .build();

        picker.addOnPositiveButtonClickListener(millis -> {
            // O picker devolve meia-noite UTC; ler em UTC evita perder um dia.
            data = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
            atualizarRotuloDaData();
        });

        picker.show(getParentFragmentManager(), "data");
    }
}
