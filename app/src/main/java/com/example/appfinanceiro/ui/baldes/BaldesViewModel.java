package com.example.appfinanceiro.ui.baldes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appfinanceiro.AppFinanceiroApplication;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.repository.BaldeRepository;

import java.util.List;

public class BaldesViewModel extends AndroidViewModel {

    private final BaldeRepository repo;
    private final LiveData<List<Balde>> baldes;

    public BaldesViewModel(@NonNull Application app) {
        super(app);
        repo = AppFinanceiroApplication.from(app).baldeRepository();
        baldes = repo.observarAtivos();
    }

    public LiveData<List<Balde>> baldes() {
        return baldes;
    }
}
