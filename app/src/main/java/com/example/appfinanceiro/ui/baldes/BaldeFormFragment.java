package com.example.appfinanceiro.ui.baldes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.appfinanceiro.R;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.entity.TipoBalde;
import com.example.appfinanceiro.domain.ValidacaoBalde;
import com.example.appfinanceiro.ui.comum.MascaraMoedaWatcher;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Criação e edição de balde. Id {@link #ID_NOVO} significa balde novo. */
public class BaldeFormFragment extends BottomSheetDialogFragment {

    public static final String ARG_BALDE_ID = "baldeId";
    public static final long ID_NOVO = 0L;

    private BaldeFormViewModel vm;
    private MascaraMoedaWatcher mascara;

    private TextView titulo;
    private TextInputLayout nomeLayout;
    private TextInputEditText nome;
    private MaterialAutoCompleteTextView tipo;
    private TextInputEditText valor;
    private MaterialSwitch rolaSaldo;
    private TextView ajudaRolaSaldo;
    private MaterialButton arquivar;

    private long baldeId = ID_NOVO;
    private TipoBalde tipoSelecionado = TipoBalde.LIVRE;

    /** Balde carregado do banco; null enquanto não chega ou se for criação. */
    private Balde original;
    /** O LiveData reemite a cada escrita; os campos só podem ser populados uma vez. */
    private boolean camposPopulados;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup pai,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_balde_form, pai, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);

        vm = new ViewModelProvider(this).get(BaldeFormViewModel.class);
        baldeId = getArguments() == null ? ID_NOVO : getArguments().getLong(ARG_BALDE_ID, ID_NOVO);

        titulo = v.findViewById(R.id.titulo_form);
        nomeLayout = v.findViewById(R.id.campo_nome_layout);
        nome = v.findViewById(R.id.campo_nome);
        tipo = v.findViewById(R.id.campo_tipo);
        valor = v.findViewById(R.id.campo_valor);
        rolaSaldo = v.findViewById(R.id.switch_rola_saldo);
        ajudaRolaSaldo = v.findViewById(R.id.ajuda_rola_saldo);
        arquivar = v.findViewById(R.id.botao_arquivar);

        mascara = new MascaraMoedaWatcher(valor);
        valor.addTextChangedListener(mascara);
        mascara.setCentavos(0);

        configurarDropdownDeTipo();

        rolaSaldo.setOnCheckedChangeListener((b, ligado) -> atualizarAjudaRolaSaldo(ligado));
        atualizarAjudaRolaSaldo(rolaSaldo.isChecked());

        nome.addTextChangedListener(new LimpaErro(nomeLayout));

        v.findViewById(R.id.botao_salvar).setOnClickListener(x -> salvar());
        arquivar.setOnClickListener(x -> confirmarArquivamento());

        if (baldeId == ID_NOVO) {
            titulo.setText(R.string.novo_balde);
            arquivar.setVisibility(View.GONE);
        } else {
            titulo.setText(R.string.editar_balde);
            arquivar.setVisibility(View.VISIBLE);
            vm.carregar(baldeId).observe(getViewLifecycleOwner(), this::popularCampos);
        }
    }

    private void configurarDropdownDeTipo() {
        TipoBalde[] tipos = TipoBalde.values();
        String[] rotulos = new String[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            rotulos[i] = getString(TipoBaldeUi.rotulo(tipos[i]));
        }
        // Os índices batem com TipoBalde.values() por construção.
        tipo.setSimpleItems(rotulos);
        tipo.setOnItemClickListener((pai, view, posicao, id) -> tipoSelecionado = tipos[posicao]);
        selecionarTipo(tipoSelecionado);
    }

    private void selecionarTipo(TipoBalde t) {
        tipoSelecionado = t;
        // false: não filtrar a lista com o texto que acabamos de escrever.
        tipo.setText(getString(TipoBaldeUi.rotulo(t)), false);
    }

    private void popularCampos(@Nullable Balde b) {
        if (b == null || camposPopulados) return;
        camposPopulados = true;

        original = b;
        nome.setText(b.getNome());
        selecionarTipo(b.getTipo());
        mascara.setCentavos(b.getValorMensalCentavos());
        rolaSaldo.setChecked(b.isRolaSaldo());
        atualizarAjudaRolaSaldo(b.isRolaSaldo());
    }

    private void atualizarAjudaRolaSaldo(boolean ligado) {
        ajudaRolaSaldo.setText(ligado
                ? R.string.ajuda_rola_saldo_ligado
                : R.string.ajuda_rola_saldo_desligado);
    }

    private void salvar() {
        String texto = nome.getText() == null ? "" : nome.getText().toString();
        long centavos = mascara.getCentavos();

        switch (ValidacaoBalde.validar(texto, centavos)) {
            case NOME_VAZIO:
                nomeLayout.setError(getString(R.string.erro_nome_vazio));
                return;
            case NOME_LONGO:
                nomeLayout.setError(getString(R.string.erro_nome_longo, ValidacaoBalde.MAX_NOME));
                return;
            case VALOR_NEGATIVO:
                // A máscara não produz negativo; se chegar aqui, é bug e não digitação.
                return;
            case NENHUM:
                break;
        }

        // Editar preserva saldo acumulado, cor e ordem — só o formulário muda.
        Balde b = original != null ? original : new Balde();
        b.setNome(texto.trim());
        b.setTipo(tipoSelecionado);
        b.setValorMensalCentavos(centavos);
        b.setRolaSaldo(rolaSaldo.isChecked());
        if (original == null) {
            b.setCorHex(TipoBaldeUi.corPadrao(tipoSelecionado));
        }

        vm.salvar(b);
        dismiss();
    }

    private void confirmarArquivamento() {
        String rotulo = original != null ? original.getNome() : "";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.arquivar_confirmar_titulo, rotulo))
                .setMessage(R.string.arquivar_confirmar_texto)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.arquivar, (d, w) -> {
                    vm.arquivar(baldeId);
                    Toast.makeText(requireContext(),
                            getString(R.string.balde_arquivado, rotulo),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .show();
    }

    /** Apaga a mensagem de erro assim que o usuário corrige o campo. */
    private static class LimpaErro implements android.text.TextWatcher {
        private final TextInputLayout layout;

        LimpaErro(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            if (layout.getError() != null) layout.setError(null);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
    }
}
