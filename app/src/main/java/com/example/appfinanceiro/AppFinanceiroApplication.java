package com.example.appfinanceiro;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.appfinanceiro.data.db.AppDatabase;
import com.example.appfinanceiro.data.db.Seed;
import com.example.appfinanceiro.data.repository.BaldeRepository;
import com.example.appfinanceiro.data.repository.TransacaoRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Composição de dependências manuais: DB singleton, IO executor e repositórios.
 * Rodar seed no callback {@link RoomDatabase.Callback#onCreate} garante que
 * os 4 baldes da seção 8 apareçam na primeira abertura, uma única vez.
 */
public class AppFinanceiroApplication extends Application {

    private ExecutorService io;
    private AppDatabase database;
    private BaldeRepository baldeRepository;
    private TransacaoRepository transacaoRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        io = Executors.newFixedThreadPool(4);

        database = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.NAME)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        io.execute(() -> Seed.popularBaldes(database.baldeDao()));
                    }
                })
                .build();

        baldeRepository = new BaldeRepository(database.baldeDao(), database.transacaoDao(), io);
        transacaoRepository = new TransacaoRepository(database.transacaoDao(), io);
    }

    public AppDatabase database() { return database; }
    public ExecutorService io() { return io; }
    public BaldeRepository baldeRepository() { return baldeRepository; }
    public TransacaoRepository transacaoRepository() { return transacaoRepository; }

    public static AppFinanceiroApplication from(android.content.Context ctx) {
        return (AppFinanceiroApplication) ctx.getApplicationContext();
    }
}
