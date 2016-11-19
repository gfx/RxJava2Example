package com.github.gfx.rxjava2example;

import com.github.gfx.rxjava2example.databinding.ActivityMainBinding;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
    }

    public void runCompletable() {
        Log.d(TAG, "runCompletable");

        Completable.fromRunnable(() -> Log.d(TAG, "task on " + Thread.currentThread()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe " + d + " on " + Thread.currentThread());
                    }

                    @Override
                    public void onNext(Object value) {
                        Log.d(TAG, "onNext " + value + " on " + Thread.currentThread());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError on " + Thread.currentThread());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete on " + Thread.currentThread());
                    }
                });
    }

    public void runObservable() {
        apiClient.getPostsAsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {
                    Log.d(TAG, posts.toString());
                });
    }

    public void runFlowables() {
        Flowable.range(1, 10)
                .flatMap(apiClient::getPostAsFlowable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    Subscription s;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Post post) {
                        Log.d(TAG, post.toString());
                        s.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.wtf(TAG, t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
                    }
                });
    }
}
