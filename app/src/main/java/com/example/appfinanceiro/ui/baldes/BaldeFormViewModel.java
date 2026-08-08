package com.example.appfinanceiro.ui.baldes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appfinanceiro.AppFinanceiroApplication;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.repository.BaldeRepository;

public class BaldeFormViewModel extends AndroidViewModel {

    private final BaldeRepository repo;

    public BaldeFormViewModel(@NonNull Application app) {
        super(app);
        repo = AppFinanceiroApplication.from(app).baldeRepository();
    }

    public LiveData<Balde> carregar(long id) {
        return repo.observarPorId(id);
    }

    /** Id zero é balde novo — a distinção que o Room usa para autoGenerate. */
    public void salvar(Balde b) {
        if (b.getId() == 0L) {
            repo.inserirNoFim(b);
        } else {
            repo.atualizar(b);
        }
    }

    public void arquivar(long id) {
        repo.arquivar(id);
    }
}
