package com.github.gfx.rxjava2example;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.github.gfx.rxjava2example.databinding.ActivityMainBinding;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    static final String TAG = MainActivity.class.getSimpleName();

    // see https://jsonplaceholder.typicode.com/
    final JsonPlaceholderService apiClient = new Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JsonPlaceholderService.class);

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        setupTextWatcher();
    }

    public void runCompletable(View button) {
        button.setEnabled(false);
        Log.d(TAG, "runCompletable");

        Completable.fromRunnable(() -> Log.d(TAG, "task on " + Thread.currentThread()))
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "onComplete");
                    button.setEnabled(true);
                });
    }

    public void runObservable(View button) {
        button.setEnabled(false);

        // parallel requests
        Observable.just(1, 2, 3, 3, 4)
                .flatMapSingle((id) -> {
                    // run on a scheduler for each request
                    return apiClient.getPostAsSingle(id)
                            .subscribeOn(Schedulers.io());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(post -> {
                    Log.d(TAG, post.toString());
                }, (err) -> {
                    Log.wtf(TAG, err);
                }, () -> {
                    button.setEnabled(true);
                });
    }

    public void runBenchmark(View button) {
        button.setEnabled(false);

        System.gc();

        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        Completable.fromRunnable(() -> {
            long t0 = System.nanoTime();
            long total = 0;

            for (int i = 0; i < 1000; i++) {
                for (int value : list) {
                    total += value;
                }
            }

            Log.d(TAG, "foreach: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0) + "ms");

            t0 = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                total += Observable.fromIterable(list)
                        .reduce(0L, (t, value) -> t + value).blockingGet();
            }

            Log.d(TAG, "RxJava2: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0) + "ms");

            t0 = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                total += rx.Observable.from(list)
                        .reduce(0L, (t, value) -> t + value).toBlocking().single();
            }

            Log.d(TAG, "RxJava1: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0) + "ms");

            t0 = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                total += com.annimon.stream.Stream.of(list)
                        .reduce(0L, (t, value) -> t + value);
            }

            Log.d(TAG, "LSA:     " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0) + "ms");

            Log.d(TAG, "(total=" + total + ")");
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> button.setEnabled(true));
    }

    private void setupTextWatcher() {
        Observable<CharSequence> events = Observable.create((source) -> {
            binding.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    source.onNext(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        });

        events.debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()) // debounce() runs on another thread
                .subscribe((s) -> {
                    Log.d(TAG, s.toString());
                    binding.output.setText(s);
                });
    }

}
